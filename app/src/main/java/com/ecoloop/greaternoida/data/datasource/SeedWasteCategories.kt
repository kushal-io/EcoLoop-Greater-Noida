package com.ecoloop.greaternoida.data.datasource

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.WasteCategory

object SeedWasteCategories {
    val categories: List<WasteCategory> = listOf(
        WasteCategory(
            id = EcoConstants.WASTE_TYPE_E_WASTE,
            name = "E-Waste",
            description = "Discarded electronics, batteries, circuit boards, chargers, and appliances",
            pointsPerKg = EcoConstants.POINTS_PER_KG_E_WASTE,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_E_WASTE,
            icon = "devices"
        ),
        WasteCategory(
            id = EcoConstants.WASTE_TYPE_PLASTIC,
            name = "Plastic",
            description = "Plastic bottles, rigid containers, clean packaging polythene",
            pointsPerKg = EcoConstants.POINTS_PER_KG_PLASTIC,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_PLASTIC,
            icon = "recycling"
        ),
        WasteCategory(
            id = EcoConstants.WASTE_TYPE_PAPER,
            name = "Paper & Cardboard",
            description = "Cardboard cartons, newspapers, study books, office paper",
            pointsPerKg = EcoConstants.POINTS_PER_KG_PAPER,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_PAPER,
            icon = "description"
        ),
        WasteCategory(
            id = EcoConstants.WASTE_TYPE_METAL,
            name = "Metal",
            description = "Beverage cans, tin boxes, steel scrap, copper, aluminum containers",
            pointsPerKg = EcoConstants.POINTS_PER_KG_METAL,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_METAL,
            icon = "hardware"
        ),
        WasteCategory(
            id = EcoConstants.WASTE_TYPE_GLASS,
            name = "Glass",
            description = "Glass bottles, jars, broken cullet, transparent glass containers",
            pointsPerKg = EcoConstants.POINTS_PER_KG_GLASS,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_GLASS,
            icon = "wine_bar"
        )
    )
}
