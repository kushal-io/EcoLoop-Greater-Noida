package com.ecoloop.greaternoida.domain.model

import com.ecoloop.greaternoida.core.constants.EcoConstants

data class WasteTransaction(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val partnerId: String = "",
    val partnerName: String = "",
    val categoryId: String = "",
    val quantityKg: Double = 0.0,
    val status: String = EcoConstants.STATUS_PENDING,
    val pointsAwarded: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val collectorId: String? = null
) {
    val isPending: Boolean
        get() = status.equals(EcoConstants.STATUS_PENDING, ignoreCase = true)

    val isConfirmed: Boolean
        get() = status.equals(EcoConstants.STATUS_CONFIRMED, ignoreCase = true)
}
