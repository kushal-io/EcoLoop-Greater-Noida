package com.ecoloop.greaternoida.domain.model

data class Reward(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val partnerName: String = "",
    val pointsCost: Int = 0,
    val category: String = "",
    val discountText: String = "",
    val terms: String = "",
    val available: Boolean = true
)
