package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.repository.PartnerRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetRecommendedCentresUseCaseTest {

    private val fakePartners = listOf(
        Partner(
            id = "partner_pari",
            name = "Pari Chowk Metro EcoPoint",
            address = "Pari Chowk",
            lat = 28.4673,
            lng = 77.5140,
            wasteTypesAccepted = listOf("e-waste", "plastic", "paper"),
            verified = true
        ),
        Partner(
            id = "partner_alpha",
            name = "Alpha-2 CleanTech Depot",
            address = "Sector Alpha 2",
            lat = 28.4782,
            lng = 77.5050,
            wasteTypesAccepted = listOf("e-waste"),
            verified = true
        ),
        Partner(
            id = "partner_metal_suraj",
            name = "Surajpur Scrap & Metal Works",
            address = "Surajpur Industrial Area",
            lat = 28.5150,
            lng = 77.4980,
            wasteTypesAccepted = listOf("metal", "glass"),
            verified = false
        ),
        Partner(
            id = "partner_beta",
            name = "Beta-1 Green Center",
            address = "Sector Beta 1",
            lat = 28.4830,
            lng = 77.5085,
            wasteTypesAccepted = listOf("plastic", "paper", "glass"),
            verified = true
        )
    )

    private val fakeRepo = object : PartnerRepository {
        override fun getPartners(): Flow<Resource<List<Partner>>> =
            flowOf(Resource.Success(fakePartners))

        override suspend fun getPartnerById(partnerId: String): Resource<Partner> =
            fakePartners.find { it.id == partnerId }?.let { Resource.Success(it) }
                ?: Resource.Error("Not found")
    }

    private val useCase = GetRecommendedCentresUseCase(fakeRepo)

    @Test
    fun returnsOnlyCentresAcceptingTargetCategory() = runTest {
        val result = useCase(EcoConstants.WASTE_TYPE_PLASTIC, userLat = 28.4670, userLng = 77.5140)
        assertThat(result).isInstanceOf(Resource.Success::class.java)

        val recommended = (result as Resource.Success).data
        assertThat(recommended).isNotEmpty()

        // Every recommended centre must accept plastic
        recommended.forEach { partner ->
            assertThat(partner.wasteTypesAccepted).contains("plastic")
        }

        // Alpha depot only accepts e-waste, so it MUST NOT be included
        assertThat(recommended.map { it.id }).doesNotContain("partner_alpha")
        assertThat(recommended.map { it.id }).doesNotContain("partner_metal_suraj")
    }

    @Test
    fun sortsCentresNearestFirst_whenUserLocationIsProvided() = runTest {
        // User is at Pari Chowk (28.4673, 77.5140)
        val result = useCase(EcoConstants.WASTE_TYPE_PLASTIC, userLat = 28.4673, userLng = 77.5140)
        val recommended = (result as Resource.Success).data

        assertThat(recommended.size).isAtLeast(2)
        // First partner should be Pari Chowk (0.0 km)
        assertThat(recommended[0].id).isEqualTo("partner_pari")
        assertThat(recommended[0].distanceKm).isLessThan(0.2)

        // Second partner should be farther away
        assertThat(recommended[1].distanceKm).isGreaterThan(recommended[0].distanceKm!!)
    }

    @Test
    fun usesFallbackLocality_whenGpsCoordinatesAreNull() = runTest {
        val result = useCase(
            categorySlug = EcoConstants.WASTE_TYPE_PLASTIC,
            userLat = null,
            userLng = null,
            fallbackLocality = "Pari Chowk"
        )
        val recommended = (result as Resource.Success).data
        assertThat(recommended).isNotEmpty()
        // Pari Chowk partner should have distance calculated relative to Pari Chowk center
        assertThat(recommended[0].id).isEqualTo("partner_pari")
        assertThat(recommended[0].distanceKm).isNotNull()
    }

    @Test
    fun returnsEmptyList_whenNoCentreAcceptsCategory() = runTest {
        val emptyRepo = object : PartnerRepository {
            override fun getPartners(): Flow<Resource<List<Partner>>> =
                flowOf(Resource.Success(fakePartners.filter { !it.wasteTypesAccepted.contains("hazardous-chemicals") }))
            override suspend fun getPartnerById(partnerId: String): Resource<Partner> = Resource.Error("Not found")
        }
        val emptyUseCase = GetRecommendedCentresUseCase(emptyRepo)

        val result = emptyUseCase("hazardous-chemicals", userLat = 28.4670, userLng = 77.5140)
        val recommended = (result as Resource.Success).data
        assertThat(recommended).isEmpty()
    }

    @Test
    fun returnsError_whenCategoryIsBlank() = runTest {
        val result = useCase("   ")
        assertThat(result).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun limitsResultsToAtMostFiveCentres() = runTest {
        // Create 8 plastic partners
        val manyPartners = (1..8).map { i ->
            Partner(
                id = "partner_$i",
                name = "Station $i",
                address = "Address $i",
                lat = 28.4673 + (i * 0.01),
                lng = 77.5140 + (i * 0.01),
                wasteTypesAccepted = listOf("plastic")
            )
        }
        val manyRepo = object : PartnerRepository {
            override fun getPartners(): Flow<Resource<List<Partner>>> = flowOf(Resource.Success(manyPartners))
            override suspend fun getPartnerById(partnerId: String): Resource<Partner> = Resource.Error("")
        }
        val manyUseCase = GetRecommendedCentresUseCase(manyRepo)

        val result = manyUseCase(EcoConstants.WASTE_TYPE_PLASTIC, userLat = 28.4673, userLng = 77.5140)
        val recommended = (result as Resource.Success).data
        assertThat(recommended.size).isEqualTo(5)
    }
}
