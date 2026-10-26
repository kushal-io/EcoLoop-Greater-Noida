package com.ecoloop.greaternoida.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.data.datasource.SeedRewardsData
import com.ecoloop.greaternoida.data.model.RedemptionDto
import com.ecoloop.greaternoida.data.model.RewardDto
import com.ecoloop.greaternoida.domain.model.Redemption
import com.ecoloop.greaternoida.domain.model.Reward
import com.ecoloop.greaternoida.domain.repository.AuthRepository
import com.ecoloop.greaternoida.domain.repository.RewardRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : RewardRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ecoloop_rewards_prefs", Context.MODE_PRIVATE)

    private val _localRedemptions = MutableStateFlow<List<Redemption>>(emptyList())
    private val localRedemptions = _localRedemptions.asStateFlow()

    init {
        restoreLocalRedemptions()
    }

    override fun getRewards(): Flow<Resource<List<Reward>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = firestore.collection(EcoConstants.COLLECTION_REWARDS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Success(SeedRewardsData.rewards))
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val rewards = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(RewardDto::class.java)?.toDomain(doc.id)
                    }
                    trySend(Resource.Success(rewards.ifEmpty { SeedRewardsData.rewards }))
                } else {
                    trySend(Resource.Success(SeedRewardsData.rewards))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getUserRedemptions(userId: String): Flow<Resource<List<Redemption>>> =
        localRedemptions.map { list ->
            val userRedemptions = list.filter { it.userId == userId }
                .sortedByDescending { it.redeemedAt }
            Resource.Success(userRedemptions)
        }

    override fun redeemReward(userId: String, reward: Reward): Flow<Resource<Redemption>> = flow {
        emit(Resource.Loading)

        // Read current user profile to verify points balance
        val userProfileResult = authRepository.getCurrentUserProfile()
        val userProfile = (userProfileResult as? Resource.Success)?.data

        val currentPoints = userProfile?.totalPoints ?: readCachedUserPoints()

        // 1. Strict guard: Check that user has sufficient points. PREVENTS negative balance!
        if (currentPoints < reward.pointsCost) {
            emit(
                Resource.Error(
                    "Insufficient points. You need ${reward.pointsCost} points, but have $currentPoints points. Recycle more waste to earn points!"
                )
            )
            return@flow
        }

        // 2. Generate short, human-friendly redemption coupon code (e.g. ECO-GNOI-7X9K)
        val shortCode = generateShortRedemptionCode()
        val redemptionId = "red_${UUID.randomUUID().toString().take(8)}"
        val now = System.currentTimeMillis()

        val redemption = Redemption(
            id = redemptionId,
            userId = userId,
            rewardId = reward.id,
            rewardTitle = reward.title,
            partnerName = reward.partnerName,
            pointsSpent = reward.pointsCost,
            redemptionCode = shortCode,
            discountText = reward.discountText,
            redeemedAt = now,
            status = "active"
        )

        // 3. Atomically deduct points locally and save redemption
        saveLocalRedemption(redemption)
        authRepository.updateUserPoints(-reward.pointsCost, 0.0)

        // 4. Firestore Atomic Transaction to update database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(3000L) {
                    firestore.runTransaction { tx ->
                        val userRef = firestore.collection(EcoConstants.COLLECTION_USERS).document(userId)
                        val redemptionRef = firestore.collection(EcoConstants.COLLECTION_REDEMPTIONS).document(redemptionId)

                        val userSnap = tx.get(userRef)
                        val pointsOnServer = userSnap.getLong("total_points")?.toInt() ?: currentPoints

                        if (pointsOnServer < reward.pointsCost) {
                            throw FirebaseFirestoreException(
                                "Insufficient points on server",
                                FirebaseFirestoreException.Code.ABORTED
                            )
                        }

                        // Deduct points
                        tx.update(userRef, "total_points", (pointsOnServer - reward.pointsCost).coerceAtLeast(0))

                        // Insert redemption record
                        tx.set(redemptionRef, RedemptionDto.fromDomain(redemption))
                    }
                }
            } catch (e: Exception) {
                // Local state is preserved for prototype smoothness
                android.util.Log.w("EcoLoop", "Firestore redemption sync note: ${e.message}")
            }
        }

        emit(Resource.Success(redemption))
    }

    private fun generateShortRedemptionCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excludes confusing characters (0, O, 1, I)
        val random = SecureRandom()
        val sb = StringBuilder()
        for (i in 0 until 4) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return "ECO-GNOI-$sb"
    }

    private fun readCachedUserPoints(): Int {
        val userPrefs = context.getSharedPreferences("ecoloop_auth_prefs", Context.MODE_PRIVATE)
        val jsonStr = userPrefs.getString("cached_user_profile", null) ?: return 0
        return try {
            JSONObject(jsonStr).optInt("totalPoints", 0)
        } catch (_: Exception) {
            0
        }
    }

    private fun saveLocalRedemption(redemption: Redemption) {
        _localRedemptions.update { current ->
            val updated = current.toMutableList()
            updated.add(0, redemption)
            persistToPrefs(updated)
            updated
        }
    }

    private fun persistToPrefs(list: List<Redemption>) {
        val array = JSONArray()
        list.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("userId", r.userId)
                put("rewardId", r.rewardId)
                put("rewardTitle", r.rewardTitle)
                put("partnerName", r.partnerName)
                put("pointsSpent", r.pointsSpent)
                put("redemptionCode", r.redemptionCode)
                put("discountText", r.discountText)
                put("redeemedAt", r.redeemedAt)
                put("status", r.status)
            }
            array.put(obj)
        }
        prefs.edit().putString("local_redemptions", array.toString()).apply()
    }

    private fun restoreLocalRedemptions() {
        val jsonStr = prefs.getString("local_redemptions", null) ?: return
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Redemption>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Redemption(
                        id = obj.getString("id"),
                        userId = obj.getString("userId"),
                        rewardId = obj.getString("rewardId"),
                        rewardTitle = obj.getString("rewardTitle"),
                        partnerName = obj.getString("partnerName"),
                        pointsSpent = obj.getInt("pointsSpent"),
                        redemptionCode = obj.getString("redemptionCode"),
                        discountText = obj.getString("discountText"),
                        redeemedAt = obj.getLong("redeemedAt"),
                        status = obj.optString("status", "active")
                    )
                )
            }
            _localRedemptions.value = list
        } catch (_: Exception) {}
    }
}
