package com.ecoloop.greaternoida.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.data.datasource.SeedWasteCategories
import com.ecoloop.greaternoida.data.model.TransactionDto
import com.ecoloop.greaternoida.data.model.WasteCategoryDto
import com.ecoloop.greaternoida.domain.model.WasteCategory
import com.ecoloop.greaternoida.domain.model.WasteTransaction
import com.ecoloop.greaternoida.domain.repository.TransactionRepository
import com.ecoloop.greaternoida.domain.usecase.CalculatePointsUseCase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val calculatePointsUseCase: CalculatePointsUseCase,
    @ApplicationContext private val context: Context
) : TransactionRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ecoloop_tx_prefs", Context.MODE_PRIVATE)

    // Reactive in-memory state flow synchronized with local storage for instant responsiveness
    private val _localTransactions = MutableStateFlow<List<WasteTransaction>>(emptyList())
    private val localTransactions = _localTransactions.asStateFlow()

    init {
        restoreLocalTransactions()
    }

    override fun createPendingTransaction(
        userId: String,
        userName: String,
        partnerId: String,
        partnerName: String,
        categoryId: String
    ): Flow<Resource<WasteTransaction>> = flow {
        emit(Resource.Loading)
        val txId = "tx_" + UUID.randomUUID().toString().take(10)
        val transaction = WasteTransaction(
            id = txId,
            userId = userId,
            userName = userName,
            partnerId = partnerId,
            partnerName = partnerName,
            categoryId = categoryId,
            quantityKg = 0.0,
            status = EcoConstants.STATUS_PENDING,
            pointsAwarded = 0,
            createdAt = System.currentTimeMillis()
        )

        // 1. Immediately store in reactive local state
        saveLocalTransaction(transaction)

        // 2. Asynchronously upload to Firestore without blocking the UI if offline
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(2500L) {
                    val dto = TransactionDto.fromDomain(transaction)
                    firestore.collection(EcoConstants.COLLECTION_TRANSACTIONS)
                        .document(txId)
                        .set(dto)
                        .await()
                }
            } catch (e: Exception) {
                Log.w("EcoLoopTx", "Firestore sync skipped for new transaction: ${e.message}")
            }
        }

        emit(Resource.Success(transaction))
    }

    override fun getUserTransactions(userId: String): Flow<Resource<List<WasteTransaction>>> =
        localTransactions.map { list ->
            val userList = list.filter { it.userId == userId }
                .sortedByDescending { it.createdAt }
            Resource.Success(userList)
        }

    override fun getPendingTransactionsForPartner(partnerId: String): Flow<Resource<List<WasteTransaction>>> =
        localTransactions.map { list ->
            val pendingList = list.filter {
                it.partnerId == partnerId && it.isPending
            }.sortedByDescending { it.createdAt }
            Resource.Success(pendingList)
        }

    override fun confirmTransaction(
        transactionId: String,
        quantityKg: Double,
        collectorId: String
    ): Flow<Resource<WasteTransaction>> = flow {
        emit(Resource.Loading)

        // Check if transaction exists and guard against double-crediting
        val currentList = _localTransactions.value
        val existing = currentList.find { it.id == transactionId }

        if (existing == null) {
            emit(Resource.Error("Transaction not found"))
            return@flow
        }

        if (existing.isConfirmed) {
            emit(Resource.Error("Transaction already confirmed. Double-crediting prevented."))
            return@flow
        }

        // Calculate points based on category rate and hazard weight
        val category = SeedWasteCategories.categories.find { it.id == existing.categoryId }
            ?: SeedWasteCategories.categories.first()
        val pointsAwarded = calculatePointsUseCase(
            quantityKg = quantityKg,
            pointsPerKg = category.pointsPerKg,
            hazardWeight = category.hazardWeight
        )

        val now = System.currentTimeMillis()
        val confirmedTx = existing.copy(
            status = EcoConstants.STATUS_CONFIRMED,
            quantityKg = quantityKg,
            pointsAwarded = pointsAwarded,
            confirmedAt = now,
            collectorId = collectorId
        )

        // 1. Immediately update reactive state and user points
        updateLocalTransaction(confirmedTx)
        updateLocalUserPoints(existing.userId, pointsAwarded, quantityKg)

        // 2. Synchronize to Firestore atomically with a reasonable timeout
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(3000L) {
                    firestore.runTransaction { tx ->
                        val txRef = firestore.collection(EcoConstants.COLLECTION_TRANSACTIONS).document(transactionId)
                        val userRef = firestore.collection(EcoConstants.COLLECTION_USERS).document(existing.userId)

                        val txSnapshot = tx.get(txRef)
                        if (txSnapshot.exists() && txSnapshot.getString("status") == EcoConstants.STATUS_CONFIRMED) {
                            return@runTransaction
                        }

                        val userSnapshot = tx.get(userRef)
                        val currentPoints = userSnapshot.getLong("total_points")?.toInt() ?: 0
                        val currentKg = userSnapshot.getDouble("total_kg_recycled") ?: 0.0

                        tx.update(
                            txRef,
                            mapOf(
                                "status" to EcoConstants.STATUS_CONFIRMED,
                                "quantity_kg" to quantityKg,
                                "points_awarded" to pointsAwarded,
                                "confirmed_at" to now,
                                "collector_id" to collectorId
                            )
                        )

                        tx.update(
                            userRef,
                            mapOf(
                                "total_points" to (currentPoints + pointsAwarded),
                                "total_kg_recycled" to (currentKg + quantityKg)
                            )
                        )
                    }.await()
                }
            } catch (e: Exception) {
                Log.w("EcoLoopTx", "Firestore runTransaction skipped/timed out: ${e.message}")
            }
        }

        // Emit successful confirmation immediately
        emit(Resource.Success(confirmedTx))
    }

    override fun getWasteCategories(): Flow<Resource<List<WasteCategory>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = firestore.collection(EcoConstants.COLLECTION_WASTE_CATEGORIES)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(Resource.Success(SeedWasteCategories.categories))
                    return@addSnapshotListener
                }

                val categories = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(WasteCategoryDto::class.java)?.toDomain(doc.id)
                }
                trySend(Resource.Success(categories.ifEmpty { SeedWasteCategories.categories }))
            }

        awaitClose { listener.remove() }
    }

    private fun saveLocalTransaction(tx: WasteTransaction) {
        _localTransactions.update { current ->
            val updated = current.toMutableList()
            updated.removeAll { it.id == tx.id }
            updated.add(0, tx)
            persistToPrefs(updated)
            updated
        }
    }

    private fun updateLocalTransaction(tx: WasteTransaction) {
        _localTransactions.update { current ->
            val updated = current.toMutableList()
            val index = updated.indexOfFirst { it.id == tx.id }
            if (index != -1) {
                updated[index] = tx
            } else {
                updated.add(0, tx)
            }
            persistToPrefs(updated)
            updated
        }
    }

    private fun updateLocalUserPoints(userId: String, points: Int, kg: Double) {
        val userPrefs = context.getSharedPreferences("ecoloop_auth_prefs", Context.MODE_PRIVATE)
        val jsonStr = userPrefs.getString("cached_user_profile", null) ?: return
        try {
            val json = JSONObject(jsonStr)
            val currentPoints = json.optInt("totalPoints", 0)
            val currentKg = json.optDouble("totalKgRecycled", 0.0)
            json.put("totalPoints", currentPoints + points)
            json.put("totalKgRecycled", currentKg + kg)
            userPrefs.edit().putString("cached_user_profile", json.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun persistToPrefs(list: List<WasteTransaction>) {
        val array = JSONArray()
        list.forEach { tx ->
            val obj = JSONObject().apply {
                put("id", tx.id)
                put("userId", tx.userId)
                put("userName", tx.userName)
                put("partnerId", tx.partnerId)
                put("partnerName", tx.partnerName)
                put("categoryId", tx.categoryId)
                put("quantityKg", tx.quantityKg)
                put("status", tx.status)
                put("pointsAwarded", tx.pointsAwarded)
                put("createdAt", tx.createdAt)
                put("confirmedAt", tx.confirmedAt ?: 0L)
                put("collectorId", tx.collectorId ?: "")
            }
            array.put(obj)
        }
        prefs.edit().putString("local_transactions", array.toString()).apply()
    }

    private fun restoreLocalTransactions() {
        val jsonStr = prefs.getString("local_transactions", null) ?: return
        try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<WasteTransaction>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    WasteTransaction(
                        id = obj.getString("id"),
                        userId = obj.getString("userId"),
                        userName = obj.optString("userName", "Citizen"),
                        partnerId = obj.getString("partnerId"),
                        partnerName = obj.optString("partnerName", "Partner Station"),
                        categoryId = obj.getString("categoryId"),
                        quantityKg = obj.optDouble("quantityKg", 0.0),
                        status = obj.optString("status", EcoConstants.STATUS_PENDING),
                        pointsAwarded = obj.optInt("pointsAwarded", 0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        confirmedAt = obj.optLong("confirmedAt").takeIf { it > 0L },
                        collectorId = obj.optString("collectorId").takeIf { it.isNotBlank() }
                    )
                )
            }
            _localTransactions.value = list
        } catch (_: Exception) {}
    }
}
