package com.ecoloop.greaternoida.domain.model

import com.ecoloop.greaternoida.core.constants.EcoConstants

data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val sector: String = "",
    val role: String = EcoConstants.ROLE_USER,
    val partnerId: String? = null,
    val totalPoints: Int = 0,
    val totalKgRecycled: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isCollector: Boolean
        get() = role.equals(EcoConstants.ROLE_COLLECTOR, ignoreCase = true)

    val isUser: Boolean
        get() = !isCollector
}
