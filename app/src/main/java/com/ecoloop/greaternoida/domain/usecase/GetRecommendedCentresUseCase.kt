package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.LocationHelper
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.data.datasource.SeedPartnersData
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.repository.PartnerRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetRecommendedCentresUseCase @Inject constructor(
    private val partnerRepository: PartnerRepository
) {
    companion object {
        // Approximate reference coordinates for Greater Noida localities when GPS permission is denied
        val SECTOR_COORDINATES: Map<String, Pair<Double, Double>> = mapOf(
            "Alpha 1" to (28.4735 to 77.5105),
            "Alpha 2" to (28.4782 to 77.5050),
            "Beta 1" to (28.4830 to 77.5085),
            "Beta 2" to (28.4875 to 77.5020),
            "Gamma 1" to (28.4915 to 77.5125),
            "Gamma 2" to (28.4965 to 77.5070),
            "Pari Chowk" to (28.4673 to 77.5140),
            "Surajpur" to (28.5150 to 77.4980)
        )
    }

    /**
     * Finds and ranks the nearest up to 5 recycling centres that strictly accept the specified waste category.
     *
     * @param categorySlug Slug of the waste category (e.g. "plastic", "paper", "metal", "glass", "e-waste")
     * @param userLat User's GPS latitude if available
     * @param userLng User's GPS longitude if available
     * @param fallbackLocality Manual Greater Noida locality if GPS is unavailable/denied
     */
    suspend operator fun invoke(
        categorySlug: String,
        userLat: Double? = null,
        userLng: Double? = null,
        fallbackLocality: String? = null
    ): Resource<List<Partner>> {
        val cleanSlug = categorySlug.trim().lowercase()
        if (cleanSlug.isBlank()) {
            return Resource.Error("Please select a valid waste category")
        }

        return try {
            // 1. Fetch partners stream (with Seed fallback)
            val partnersResult = partnerRepository.getPartners().first()
            val allPartners = when (partnersResult) {
                is Resource.Success -> partnersResult.data.ifEmpty { SeedPartnersData.partners }
                else -> SeedPartnersData.partners
            }

            // 2. Strict Filter: ONLY partners whose wasteTypesAccepted contains this category
            val eligiblePartners = allPartners.filter { partner ->
                partner.wasteTypesAccepted.any { acceptedType ->
                    acceptedType.equals(cleanSlug, ignoreCase = true)
                }
            }

            if (eligiblePartners.isEmpty()) {
                return Resource.Success(emptyList())
            }

            // 3. Determine reference location (GPS first, then manual locality fallback)
            val effectiveLat: Double?
            val effectiveLng: Double?

            if (userLat != null && userLng != null) {
                effectiveLat = userLat
                effectiveLng = userLng
            } else if (fallbackLocality != null && SECTOR_COORDINATES.containsKey(fallbackLocality)) {
                val coords = SECTOR_COORDINATES[fallbackLocality]
                effectiveLat = coords?.first
                effectiveLng = coords?.second
            } else {
                effectiveLat = null
                effectiveLng = null
            }

            // 4. Calculate distances and sort nearest first
            val sortedPartners = if (effectiveLat != null && effectiveLng != null) {
                eligiblePartners.map { partner ->
                    val distance = LocationHelper.calculateDistanceKm(
                        lat1 = effectiveLat,
                        lng1 = effectiveLng,
                        lat2 = partner.lat,
                        lng2 = partner.lng
                    )
                    partner.copy(distanceKm = distance)
                }.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
            } else {
                // If no location or locality is provided, sort by verified status first
                eligiblePartners.sortedByDescending { it.verified }
            }

            // 5. Take top 5 nearest centres
            Resource.Success(sortedPartners.take(5))
        } catch (e: Exception) {
            Resource.Error("Failed to fetch recommended centres: ${e.localizedMessage}", e)
        }
    }
}
