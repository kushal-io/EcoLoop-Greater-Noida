package com.ecoloop.greaternoida.domain.model

data class Redemption(
    val id: String = "",
    val userId: String = "",
    val rewardId: String = "",
    val rewardTitle: String = "",
    val partnerName: String = "",
    val pointsSpent: Int = 0,
    val redemptionCode: String = "",
    val discountText: String = "",
    val redeemedAt: Long = System.currentTimeMillis(),
    val status: String = "active" // active, used
)
