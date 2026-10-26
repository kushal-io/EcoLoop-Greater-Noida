package com.ecoloop.greaternoida.domain.usecase

import kotlin.math.roundToInt

/**
 * UseCase implementing the prototype points formula:
 * points = round(quantity_kg * points_per_kg * hazard_weight)
 *
 * E-waste has a higher hazard_weight than plastic and paper, rewarding users
 * proportionally for handling high-risk environmental toxins.
 */
class CalculatePointsUseCase {

    operator fun invoke(
        quantityKg: Double,
        pointsPerKg: Int,
        hazardWeight: Double
    ): Int {
        if (quantityKg <= 0.0 || pointsPerKg <= 0 || hazardWeight <= 0.0) {
            return 0
        }
        val calculated = quantityKg * pointsPerKg * hazardWeight
        return calculated.roundToInt()
    }
}
