package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Reward
import com.ecoloop.greaternoida.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRewardsUseCase @Inject constructor(
    private val rewardRepository: RewardRepository
) {
    operator fun invoke(): Flow<Resource<List<Reward>>> {
        return rewardRepository.getRewards()
    }
}
