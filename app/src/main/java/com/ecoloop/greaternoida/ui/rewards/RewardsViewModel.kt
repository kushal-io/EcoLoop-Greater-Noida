package com.ecoloop.greaternoida.ui.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Reward
import com.ecoloop.greaternoida.domain.usecase.GetRewardsUseCase
import com.ecoloop.greaternoida.domain.usecase.GetUserRedemptionsUseCase
import com.ecoloop.greaternoida.domain.usecase.RedeemRewardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val getRewardsUseCase: GetRewardsUseCase,
    private val getUserRedemptionsUseCase: GetUserRedemptionsUseCase,
    private val redeemRewardUseCase: RedeemRewardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    init {
        loadRewards()
    }

    fun loadRewards() {
        viewModelScope.launch {
            getRewardsUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            rewards = result.data,
                            errorMessage = null
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun loadUserRedemptions(userId: String) {
        viewModelScope.launch {
            getUserRedemptionsUseCase(userId).collect { result ->
                if (result is Resource.Success) {
                    _uiState.update { it.copy(userRedemptions = result.data) }
                }
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) null else category)
        }
    }

    fun openRedeemDialog(reward: Reward) {
        _uiState.update {
            it.copy(
                rewardToRedeem = reward,
                errorMessage = null
            )
        }
    }

    fun closeRedeemDialog() {
        _uiState.update { it.copy(rewardToRedeem = null) }
    }

    fun dismissLatestRedemption() {
        _uiState.update { it.copy(latestRedemption = null) }
    }

    fun redeem(userId: String) {
        val reward = _uiState.value.rewardToRedeem ?: return

        viewModelScope.launch {
            redeemRewardUseCase(userId, reward).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isRedeeming = true) }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isRedeeming = false,
                                rewardToRedeem = null,
                                latestRedemption = result.data,
                                successMessage = "Successfully redeemed ${reward.title}!"
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isRedeeming = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
