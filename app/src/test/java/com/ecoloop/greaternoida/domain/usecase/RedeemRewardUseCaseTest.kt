package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Redemption
import com.ecoloop.greaternoida.domain.model.Reward
import com.ecoloop.greaternoida.domain.repository.RewardRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class RedeemRewardUseCaseTest {

    private val sampleReward = Reward(
        id = "rew_canteen_01",
        title = "Galgotias Canteen ₹50 Voucher",
        partnerName = "Galgotias University Canteen",
        pointsCost = 50,
        available = true
    )

    @Test
    fun returnsError_whenUserIdIsBlank() = runTest {
        val fakeRepo = object : FakeRewardRepository() {}
        val useCase = RedeemRewardUseCase(fakeRepo)

        val results = useCase("", sampleReward).toList()
        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
        assertThat((results.last() as Resource.Error).message).contains("User ID cannot be blank")
    }

    @Test
    fun returnsError_whenRewardIsUnavailable() = runTest {
        val fakeRepo = object : FakeRewardRepository() {}
        val useCase = RedeemRewardUseCase(fakeRepo)

        val unavailableReward = sampleReward.copy(available = false)
        val results = useCase("user_123", unavailableReward).toList()
        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
        assertThat((results.last() as Resource.Error).message).contains("unavailable")
    }

    @Test
    fun returnsError_whenUserHasInsufficientPoints_preventingNegativeBalance() = runTest {
        val userPoints = 30 // Cost is 50, user only has 30!
        val fakeRepo = object : FakeRewardRepository() {
            override fun redeemReward(userId: String, reward: Reward): Flow<Resource<Redemption>> = flow {
                if (userPoints < reward.pointsCost) {
                    emit(Resource.Error("Insufficient points. You need ${reward.pointsCost} points, but have $userPoints points."))
                } else {
                    emit(Resource.Success(Redemption(id = "red_1", pointsSpent = reward.pointsCost)))
                }
            }
        }
        val useCase = RedeemRewardUseCase(fakeRepo)

        val results = useCase("user_123", sampleReward).toList()
        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
        val error = results.last() as Resource.Error
        assertThat(error.message).contains("Insufficient points")
    }

    @Test
    fun returnsSuccessWithRedemptionCode_whenPointsAreSufficient() = runTest {
        var userPoints = 120
        val fakeRepo = object : FakeRewardRepository() {
            override fun redeemReward(userId: String, reward: Reward): Flow<Resource<Redemption>> = flow {
                if (userPoints >= reward.pointsCost) {
                    userPoints -= reward.pointsCost
                    emit(
                        Resource.Success(
                            Redemption(
                                id = "red_1",
                                userId = userId,
                                rewardId = reward.id,
                                rewardTitle = reward.title,
                                pointsSpent = reward.pointsCost,
                                redemptionCode = "ECO-GNOI-7X9K",
                                status = "active"
                            )
                        )
                    )
                }
            }
        }
        val useCase = RedeemRewardUseCase(fakeRepo)

        val results = useCase("user_123", sampleReward).toList()
        assertThat(results.last()).isInstanceOf(Resource.Success::class.java)
        val redemption = (results.last() as Resource.Success).data
        assertThat(redemption.pointsSpent).isEqualTo(50)
        assertThat(redemption.redemptionCode).isEqualTo("ECO-GNOI-7X9K")
        assertThat(userPoints).isEqualTo(70) // 120 - 50 = 70 points remaining, no negative balance
    }

    private open class FakeRewardRepository : RewardRepository {
        override fun getRewards(): Flow<Resource<List<Reward>>> = flow {}
        override fun getUserRedemptions(userId: String): Flow<Resource<List<Redemption>>> = flow {}
        override fun redeemReward(userId: String, reward: Reward): Flow<Resource<Redemption>> = flow {}
    }
}
