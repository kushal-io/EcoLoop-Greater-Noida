package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Redemption
import com.ecoloop.greaternoida.domain.repository.RewardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserRedemptionsUseCase @Inject constructor(
    private val rewardRepository: RewardRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<Redemption>>> {
        return rewardRepository.getUserRedemptions(userId)
    }
}
