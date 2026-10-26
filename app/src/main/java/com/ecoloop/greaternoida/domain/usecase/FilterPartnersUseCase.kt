package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.LocationHelper
import com.ecoloop.greaternoida.domain.model.Partner
import javax.inject.Inject

class FilterPartnersUseCase @Inject constructor() {

    operator fun invoke(
        partners: List<Partner>,
        selectedWasteType: String?, // null or "all" means no waste type filter
        searchQuery: String = "",
        userLat: Double? = null,
        userLng: Double? = null
    ): List<Partner> {
        val filtered = partners.filter { partner ->
            val matchesWasteType = if (selectedWasteType.isNullOrBlank() || selectedWasteType.equals("all", ignoreCase = true)) {
                true
            } else {
                partner.wasteTypesAccepted.any { it.equals(selectedWasteType, ignoreCase = true) }
            }

            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                partner.name.contains(searchQuery, ignoreCase = true) ||
                partner.address.contains(searchQuery, ignoreCase = true)
            }

            matchesWasteType && matchesSearch
        }

        // Calculate distance if user location is known
        val withDistance = if (userLat != null && userLng != null) {
            filtered.map { partner ->
                val dist = LocationHelper.calculateDistanceKm(
                    userLat, userLng,
                    partner.lat, partner.lng
                )
                partner.copy(distanceKm = dist)
            }.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
        } else {
            filtered
        }

        return withDistance
    }
}
