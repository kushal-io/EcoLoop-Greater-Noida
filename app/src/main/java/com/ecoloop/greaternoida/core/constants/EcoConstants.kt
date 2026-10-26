package com.ecoloop.greaternoida.core.constants

/**
 * Global constants for EcoLoop Greater Noida.
 * Centralizes collection names, points rates, hazard weights, and default coordinates.
 */
object EcoConstants {
    // Firestore Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PARTNERS = "partners"
    const val COLLECTION_WASTE_CATEGORIES = "waste_categories"
    const val COLLECTION_TRANSACTIONS = "transactions"
    const val COLLECTION_REWARDS = "rewards"
    const val COLLECTION_REDEMPTIONS = "redemptions"

    // Supported Waste Types
    const val WASTE_TYPE_E_WASTE = "e-waste"
    const val WASTE_TYPE_PLASTIC = "plastic"
    const val WASTE_TYPE_PAPER = "paper"
    const val WASTE_TYPE_METAL = "metal"
    const val WASTE_TYPE_GLASS = "glass"

    // Points Rates (Base points awarded per kilogram)
    const val POINTS_PER_KG_E_WASTE = 20
    const val POINTS_PER_KG_PLASTIC = 10
    const val POINTS_PER_KG_PAPER = 5
    const val POINTS_PER_KG_METAL = 15
    const val POINTS_PER_KG_GLASS = 8

    // Hazard Weights (Multiplier reflecting environmental/health risk)
    // E-waste has heavy metals/toxins -> highest hazard weight
    const val HAZARD_WEIGHT_E_WASTE = 2.5
    const val HAZARD_WEIGHT_PLASTIC = 1.2
    const val HAZARD_WEIGHT_PAPER = 1.0
    const val HAZARD_WEIGHT_METAL = 1.3
    const val HAZARD_WEIGHT_GLASS = 1.1

    // Greater Noida Geolocation Center (Near Pari Chowk / Knowledge Park)
    const val GREATER_NOIDA_CENTER_LAT = 28.4744
    const val GREATER_NOIDA_CENTER_LNG = 77.5040
    const val DEFAULT_MAP_ZOOM = 12.8f

    // Roles
    const val ROLE_USER = "user"
    const val ROLE_COLLECTOR = "collector"

    // Transaction Statuses
    const val STATUS_PENDING = "pending"
    const val STATUS_CONFIRMED = "confirmed"

    // Greater Noida Residential & Commercial Sectors
    val GREATER_NOIDA_SECTORS = listOf(
        "Alpha 1",
        "Alpha 2",
        "Beta 1",
        "Beta 2",
        "Gamma 1",
        "Gamma 2",
        "Delta 1",
        "Delta 2",
        "Pari Chowk",
        "Knowledge Park",
        "Surajpur",
        "Omega 1",
        "Chi 1",
        "Pi 1",
        "Zeta 1"
    )
}
