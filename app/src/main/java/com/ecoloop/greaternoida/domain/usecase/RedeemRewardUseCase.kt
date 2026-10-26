package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Redemption
import com.ecoloop.greaternoida.domain.model.Reward
import com.ecoloop.greaternoida.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RedeemRewardUseCase @Inject constructor(
    private val rewardRepository: RewardRepository
) {
    /**
     * Redeems a reward for the given user:
     * - Validates user ID and reward availability
     * - Verifies sufficient points balance to prevent negative balance
     * - Atomically deducts points and returns the newly generated redemption with short coupon code
     */
    operator fun invoke(userId: String, reward: Reward): Flow<Resource<Redemption>> {
        if (userId.isBlank()) {
            return flow { emit(Resource.Error("User ID cannot be blank")) }
        }
        if (!reward.available) {
            return flow { emit(Resource.Error("This reward is currently unavailable")) }
        }
        if (reward.pointsCost <= 0) {
            return flow { emit(Resource.Error("Invalid reward points cost")) }
        }
        return rewardRepository.redeemReward(userId, reward)
    }
}
