package com.ecoloop.greaternoida.domain.model

data class WasteCategory(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val pointsPerKg: Int = 0,
    val hazardWeight: Double = 1.0,
    val icon: String = ""
)
