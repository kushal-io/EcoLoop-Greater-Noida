package com.ecoloop.greaternoida.domain.model

data class Partner(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val wasteTypesAccepted: List<String> = emptyList(),
    val verified: Boolean = false,
    val qrCodeId: String = "",
    val distanceKm: Double? = null
)
