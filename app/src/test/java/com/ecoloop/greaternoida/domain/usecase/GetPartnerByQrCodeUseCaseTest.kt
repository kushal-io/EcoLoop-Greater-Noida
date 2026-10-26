package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.repository.PartnerRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetPartnerByQrCodeUseCaseTest {

    private val fakePartners = listOf(
        Partner(
            id = "pari_01",
            name = "Pari Chowk Metro EcoPoint",
            address = "Pari Chowk",
            qrCodeId = "ECO_PARI_001"
        ),
        Partner(
            id = "alpha_01",
            name = "Alpha-1 Community Recyclers",
            address = "Alpha 1 Market",
            qrCodeId = "ECO_ALPHA_001"
        )
    )

    private val fakePartnerRepository = object : PartnerRepository {
        override fun getPartners(): Flow<Resource<List<Partner>>> =
            flowOf(Resource.Success(fakePartners))

        override suspend fun getPartnerById(partnerId: String): Resource<Partner> =
            fakePartners.find { it.id == partnerId }?.let { Resource.Success(it) }
                ?: Resource.Error("Not found")
    }

    private val useCase = GetPartnerByQrCodeUseCase(fakePartnerRepository)

    @Test
    fun returnsPartner_whenExactQrCodeMatches() = runTest {
        val result = useCase("ECO_PARI_001")
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        val partner = (result as Resource.Success).data
        assertThat(partner.id).isEqualTo("pari_01")
        assertThat(partner.name).isEqualTo("Pari Chowk Metro EcoPoint")
    }

    @Test
    fun returnsPartner_whenQrCodeHasDifferentCaseOrWhitespace() = runTest {
        val result = useCase("  eco_pari_001  ")
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        val partner = (result as Resource.Success).data
        assertThat(partner.id).isEqualTo("pari_01")
    }

    @Test
    fun returnsError_whenQrCodeIsBlank() = runTest {
        val result = useCase("   ")
        assertThat(result).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun returnsError_whenQrCodeDoesNotExist() = runTest {
        val result = useCase("ECO_UNKNOWN_999")
        assertThat(result).isInstanceOf(Resource.Error::class.java)
    }
}
