package com.ecoloop.greaternoida.domain.model

import com.ecoloop.greaternoida.core.constants.EcoConstants

enum class WasteType(
    val slug: String,
    val displayName: String,
    val defaultPointsPerKg: Int,
    val defaultHazardWeight: Double
) {
    E_WASTE(
        slug = EcoConstants.WASTE_TYPE_E_WASTE,
        displayName = "E-Waste",
        defaultPointsPerKg = EcoConstants.POINTS_PER_KG_E_WASTE,
        defaultHazardWeight = EcoConstants.HAZARD_WEIGHT_E_WASTE
    ),
    PLASTIC(
        slug = EcoConstants.WASTE_TYPE_PLASTIC,
        displayName = "Plastic",
        defaultPointsPerKg = EcoConstants.POINTS_PER_KG_PLASTIC,
        defaultHazardWeight = EcoConstants.HAZARD_WEIGHT_PLASTIC
    ),
    PAPER(
        slug = EcoConstants.WASTE_TYPE_PAPER,
        displayName = "Paper",
        defaultPointsPerKg = EcoConstants.POINTS_PER_KG_PAPER,
        defaultHazardWeight = EcoConstants.HAZARD_WEIGHT_PAPER
    ),
    METAL(
        slug = EcoConstants.WASTE_TYPE_METAL,
        displayName = "Metal",
        defaultPointsPerKg = EcoConstants.POINTS_PER_KG_METAL,
        defaultHazardWeight = EcoConstants.HAZARD_WEIGHT_METAL
    ),
    GLASS(
        slug = EcoConstants.WASTE_TYPE_GLASS,
        displayName = "Glass",
        defaultPointsPerKg = EcoConstants.POINTS_PER_KG_GLASS,
        defaultHazardWeight = EcoConstants.HAZARD_WEIGHT_GLASS
    );

    companion object {
        fun fromSlug(slug: String): WasteType? {
            return entries.find { it.slug.equals(slug, ignoreCase = true) }
        }
    }
}
