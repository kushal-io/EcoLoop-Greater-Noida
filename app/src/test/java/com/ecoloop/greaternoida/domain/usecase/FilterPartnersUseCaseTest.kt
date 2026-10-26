package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.data.datasource.SeedPartnersData
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class FilterPartnersUseCaseTest {

    private lateinit var filterPartnersUseCase: FilterPartnersUseCase
    private val allPartners = SeedPartnersData.partners

    @Before
    fun setUp() {
        filterPartnersUseCase = FilterPartnersUseCase()
    }

    @Test
    fun `null or all waste type returns all 15 partners`() {
        val filteredNull = filterPartnersUseCase(allPartners, selectedWasteType = null)
        val filteredAll = filterPartnersUseCase(allPartners, selectedWasteType = "all")

        assertThat(filteredNull).hasSize(15)
        assertThat(filteredAll).hasSize(15)
    }

    @Test
    fun `e-waste filter returns only partners accepting e-waste`() {
        val eWastePartners = filterPartnersUseCase(allPartners, selectedWasteType = EcoConstants.WASTE_TYPE_E_WASTE)

        assertThat(eWastePartners).isNotEmpty()
        eWastePartners.forEach { partner ->
            assertThat(partner.wasteTypesAccepted).contains(EcoConstants.WASTE_TYPE_E_WASTE)
        }
    }

    @Test
    fun `search query filters by sector name accurately`() {
        val parietalPartners = filterPartnersUseCase(allPartners, selectedWasteType = null, searchQuery = "Pari Chowk")

        assertThat(parietalPartners).isNotEmpty()
        parietalPartners.forEach { partner ->
            val matches = partner.name.contains("Pari Chowk", ignoreCase = true) ||
                          partner.address.contains("Pari Chowk", ignoreCase = true)
            assertThat(matches).isTrue()
        }
    }

    @Test
    fun `search query and waste type filter compound properly`() {
        // Find partners in "Alpha" accepting "e-waste"
        val alphaEWaste = filterPartnersUseCase(
            allPartners,
            selectedWasteType = EcoConstants.WASTE_TYPE_E_WASTE,
            searchQuery = "Alpha"
        )

        assertThat(alphaEWaste).isNotEmpty()
        alphaEWaste.forEach { partner ->
            assertThat(partner.wasteTypesAccepted).contains(EcoConstants.WASTE_TYPE_E_WASTE)
            val matchesAlpha = partner.name.contains("Alpha", ignoreCase = true) ||
                               partner.address.contains("Alpha", ignoreCase = true)
            assertThat(matchesAlpha).isTrue()
        }
    }

    @Test
    fun `sorting by distance works when user location provided`() {
        // User at Pari Chowk (28.4673, 77.5140)
        val sorted = filterPartnersUseCase(
            allPartners,
            selectedWasteType = null,
            userLat = 28.4673,
            userLng = 77.5140
        )

        assertThat(sorted).isNotEmpty()
        val first = sorted.first()
        assertThat(first.distanceKm).isNotNull()
        assertThat(first.distanceKm).isAtMost(0.5) // Pari Chowk Metro should be ~0.0km away
        assertThat(first.id).isEqualTo("partner_pari_01")
    }
}
