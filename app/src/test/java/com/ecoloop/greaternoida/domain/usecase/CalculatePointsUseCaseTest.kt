package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class CalculatePointsUseCaseTest {

    private lateinit var calculatePointsUseCase: CalculatePointsUseCase

    @Before
    fun setUp() {
        calculatePointsUseCase = CalculatePointsUseCase()
    }

    @Test
    fun `e-waste 2kg with hazard weight awards expected high points`() {
        // E-waste: 20 pts/kg * 2.5 hazard weight = 50 points per kg
        // 2.0 kg -> 100 points
        val points = calculatePointsUseCase(
            quantityKg = 2.0,
            pointsPerKg = EcoConstants.POINTS_PER_KG_E_WASTE,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_E_WASTE
        )

        assertThat(points).isEqualTo(100)
    }

    @Test
    fun `plastic 5kg calculates correctly with 1_2 hazard weight`() {
        // Plastic: 10 pts/kg * 1.2 hazard weight = 12 points per kg
        // 5.0 kg -> 60 points
        val points = calculatePointsUseCase(
            quantityKg = 5.0,
            pointsPerKg = EcoConstants.POINTS_PER_KG_PLASTIC,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_PLASTIC
        )

        assertThat(points).isEqualTo(60)
    }

    @Test
    fun `paper 10kg calculates correctly with 1_0 hazard weight`() {
        // Paper: 5 pts/kg * 1.0 hazard weight = 5 points per kg
        // 10.0 kg -> 50 points
        val points = calculatePointsUseCase(
            quantityKg = 10.0,
            pointsPerKg = EcoConstants.POINTS_PER_KG_PAPER,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_PAPER
        )

        assertThat(points).isEqualTo(50)
    }

    @Test
    fun `e-waste awards more points than same weight of plastic and paper`() {
        val qty = 3.0
        val eWastePoints = calculatePointsUseCase(
            quantityKg = qty,
            pointsPerKg = EcoConstants.POINTS_PER_KG_E_WASTE,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_E_WASTE
        )
        val plasticPoints = calculatePointsUseCase(
            quantityKg = qty,
            pointsPerKg = EcoConstants.POINTS_PER_KG_PLASTIC,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_PLASTIC
        )
        val paperPoints = calculatePointsUseCase(
            quantityKg = qty,
            pointsPerKg = EcoConstants.POINTS_PER_KG_PAPER,
            hazardWeight = EcoConstants.HAZARD_WEIGHT_PAPER
        )

        // 3.0 * 20 * 2.5 = 150
        // 3.0 * 10 * 1.2 = 36
        // 3.0 * 5 * 1.0 = 15
        assertThat(eWastePoints).isGreaterThan(plasticPoints)
        assertThat(plasticPoints).isGreaterThan(paperPoints)
    }

    @Test
    fun `fractional kilogram quantity rounds to nearest integer correctly`() {
        // 1.55 kg * 10 * 1.2 = 18.6 -> rounded to 19
        val points = calculatePointsUseCase(
            quantityKg = 1.55,
            pointsPerKg = 10,
            hazardWeight = 1.2
        )

        assertThat(points).isEqualTo(19)
    }

    @Test
    fun `zero or negative quantity returns zero points`() {
        assertThat(calculatePointsUseCase(0.0, 20, 2.5)).isEqualTo(0)
        assertThat(calculatePointsUseCase(-5.0, 20, 2.5)).isEqualTo(0)
    }

    @Test
    fun `invalid zero rate or hazard weight returns zero points`() {
        assertThat(calculatePointsUseCase(5.0, 0, 2.5)).isEqualTo(0)
        assertThat(calculatePointsUseCase(5.0, 20, 0.0)).isEqualTo(0)
        assertThat(calculatePointsUseCase(5.0, 20, -1.0)).isEqualTo(0)
    }
}
