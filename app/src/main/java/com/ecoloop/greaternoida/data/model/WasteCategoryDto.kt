package com.ecoloop.greaternoida.data.model

import com.ecoloop.greaternoida.domain.model.WasteCategory
import com.google.firebase.firestore.PropertyName

data class WasteCategoryDto(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    @get:PropertyName("points_per_kg")
    @set:PropertyName("points_per_kg")
    var pointsPerKg: Int = 0,
    @get:PropertyName("hazard_weight")
    @set:PropertyName("hazard_weight")
    var hazardWeight: Double = 1.0,
    var icon: String = ""
) {
    fun toDomain(documentId: String = id): WasteCategory {
        return WasteCategory(
            id = documentId.ifBlank { id },
            name = name,
            description = description,
            pointsPerKg = pointsPerKg,
            hazardWeight = hazardWeight,
            icon = icon
        )
    }
}
