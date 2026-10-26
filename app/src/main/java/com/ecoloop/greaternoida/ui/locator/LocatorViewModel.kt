package com.ecoloop.greaternoida.ui.locator

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.usecase.FilterPartnersUseCase
import com.ecoloop.greaternoida.domain.usecase.GetPartnersUseCase
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocatorViewModel @Inject constructor(
    private val getPartnersUseCase: GetPartnersUseCase,
    private val filterPartnersUseCase: FilterPartnersUseCase,
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocatorUiState())
    val uiState: StateFlow<LocatorUiState> = _uiState.asStateFlow()

    init {
        loadPartners()
    }

    fun loadPartners() {
        viewModelScope.launch {
            getPartnersUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update { currentState ->
                            val filtered = filterPartnersUseCase(
                                partners = result.data,
                                selectedWasteType = currentState.selectedWasteType,
                                searchQuery = currentState.searchQuery,
                                userLat = currentState.userLat,
                                userLng = currentState.userLng
                            )
                            currentState.copy(
                                isLoading = false,
                                partners = result.data,
                                filteredPartners = filtered,
                                errorMessage = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun selectWasteType(wasteType: String?) {
        _uiState.update { currentState ->
            val newSelection = if (currentState.selectedWasteType == wasteType) null else wasteType
            val filtered = filterPartnersUseCase(
                partners = currentState.partners,
                selectedWasteType = newSelection,
                searchQuery = currentState.searchQuery,
                userLat = currentState.userLat,
                userLng = currentState.userLng
            )
            currentState.copy(
                selectedWasteType = newSelection,
                filteredPartners = filtered
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            val filtered = filterPartnersUseCase(
                partners = currentState.partners,
                selectedWasteType = currentState.selectedWasteType,
                searchQuery = query,
                userLat = currentState.userLat,
                userLng = currentState.userLng
            )
            currentState.copy(
                searchQuery = query,
                filteredPartners = filtered
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isMapView = !it.isMapView) }
    }

    fun selectPartner(partner: Partner?) {
        _uiState.update { it.copy(selectedPartner = partner) }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = granted) }
        if (granted) {
            fetchUserLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _uiState.update { currentState ->
                        val filtered = filterPartnersUseCase(
                            partners = currentState.partners,
                            selectedWasteType = currentState.selectedWasteType,
                            searchQuery = currentState.searchQuery,
                            userLat = location.latitude,
                            userLng = location.longitude
                        )
                        currentState.copy(
                            userLat = location.latitude,
                            userLng = location.longitude,
                            filteredPartners = filtered
                        )
                    }
                }
            }
        } catch (_: SecurityException) {
            // Permission revoked or not granted
        }
    }

    fun setUseGoogleMaps(useGoogle: Boolean) {
        _uiState.update { it.copy(useGoogleMaps = useGoogle) }
    }

    fun retry() {
        loadPartners()
    }
}
