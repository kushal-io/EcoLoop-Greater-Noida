package com.ecoloop.greaternoida.ui.locator

import com.ecoloop.greaternoida.BuildConfig
import com.ecoloop.greaternoida.domain.model.Partner

data class LocatorUiState(
    val isLoading: Boolean = true,
    val partners: List<Partner> = emptyList(),
    val filteredPartners: List<Partner> = emptyList(),
    val selectedWasteType: String? = null, // null means "All"
    val searchQuery: String = "",
    val isMapView: Boolean = true,
    val selectedPartner: Partner? = null,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val errorMessage: String? = null,
    val isLocationPermissionGranted: Boolean = false,
    val isMapsKeyConfigured: Boolean = BuildConfig.MAPS_API_KEY.isNotBlank() && !BuildConfig.MAPS_API_KEY.contains("PLACEHOLDER"),
    val useGoogleMaps: Boolean = BuildConfig.MAPS_API_KEY.isNotBlank() && !BuildConfig.MAPS_API_KEY.contains("PLACEHOLDER")
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && filteredPartners.isEmpty()
}
