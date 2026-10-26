package com.ecoloop.greaternoida.ui.rewards

import com.ecoloop.greaternoida.domain.model.Redemption
import com.ecoloop.greaternoida.domain.model.Reward

data class RewardsUiState(
    val isLoading: Boolean = false,
    val rewards: List<Reward> = emptyList(),
    val userRedemptions: List<Redemption> = emptyList(),
    val selectedCategory: String? = null,
    val rewardToRedeem: Reward? = null,
    val latestRedemption: Redemption? = null,
    val isRedeeming: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val filteredRewards: List<Reward>
        get() = if (selectedCategory == null) {
            rewards
        } else {
            rewards.filter { it.category.contains(selectedCategory, ignoreCase = true) }
        }

    val availableCategories: List<String>
        get() = rewards.map { it.category }.distinct()
}
