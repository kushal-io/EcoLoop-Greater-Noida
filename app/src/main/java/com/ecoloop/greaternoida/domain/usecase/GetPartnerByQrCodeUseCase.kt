package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.data.datasource.SeedPartnersData
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.repository.PartnerRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetPartnerByQrCodeUseCase @Inject constructor(
    private val partnerRepository: PartnerRepository
) {
    /**
     * Resolves a Partner from a scanned QR Code ID (e.g. "ECO_PARI_001").
     * Case-insensitive and trimmed to tolerate minor scanning whitespace differences.
     */
    suspend operator fun invoke(qrCodeId: String): Resource<Partner> {
        val cleanCode = qrCodeId.trim()
        if (cleanCode.isBlank()) {
            return Resource.Error("Scanned QR Code was empty")
        }

        return try {
            // Check through current stream of partners
            val result = partnerRepository.getPartners().first()
            val partner = when (result) {
                is Resource.Success -> {
                    result.data.find { it.qrCodeId.equals(cleanCode, ignoreCase = true) }
                }
                else -> null
            } ?: SeedPartnersData.partners.find { it.qrCodeId.equals(cleanCode, ignoreCase = true) }

            if (partner != null) {
                Resource.Success(partner)
            } else {
                Resource.Error("No recycling station found for QR code: $cleanCode")
            }
        } catch (e: Exception) {
            val fallback = SeedPartnersData.partners.find { it.qrCodeId.equals(cleanCode, ignoreCase = true) }
            if (fallback != null) {
                Resource.Success(fallback)
            } else {
                Resource.Error("Error identifying station: ${e.localizedMessage}")
            }
        }
    }
}
