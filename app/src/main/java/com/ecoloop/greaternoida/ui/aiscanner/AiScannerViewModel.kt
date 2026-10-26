package com.ecoloop.greaternoida.ui.aiscanner

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.usecase.ClassifyWasteImageUseCase
import com.ecoloop.greaternoida.domain.usecase.GetRecommendedCentresUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiScannerViewModel @Inject constructor(
    private val classifyWasteImageUseCase: ClassifyWasteImageUseCase,
    private val getRecommendedCentresUseCase: GetRecommendedCentresUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AiScannerUiState(
            isModelAvailable = classifyWasteImageUseCase.isModelAvailable,
            modelStatusMessage = classifyWasteImageUseCase.modelStatusMessage
        )
    )
    val uiState: StateFlow<AiScannerUiState> = _uiState.asStateFlow()

    fun onPhotoCaptured(bitmap: Bitmap) {
        _uiState.update {
            it.copy(
                capturedBitmap = bitmap,
                uncroppedOriginalBitmap = bitmap,
                isCropping = false,
                isAnalyzing = true,
                errorMessage = null,
                classificationResult = null,
                confirmedCategorySlug = null,
                confirmedCategoryDisplayName = null,
                hasRecommendationsSearched = false,
                recommendedPartners = emptyList()
            )
        }
        analyzeBitmap(bitmap, _uiState.value.confidenceThreshold)
    }

    fun onStartCropping() {
        _uiState.update { it.copy(isCropping = true) }
    }

    fun onCancelCropping() {
        _uiState.update { it.copy(isCropping = false) }
    }

    fun onApplyCrop(croppedBitmap: Bitmap) {
        _uiState.update {
            it.copy(
                capturedBitmap = croppedBitmap,
                isCropping = false,
                isAnalyzing = true,
                errorMessage = null,
                classificationResult = null,
                confirmedCategorySlug = null,
                confirmedCategoryDisplayName = null,
                hasRecommendationsSearched = false,
                recommendedPartners = emptyList()
            )
        }
        analyzeBitmap(croppedBitmap, _uiState.value.confidenceThreshold)
    }

    fun onResetCrop() {
        val original = _uiState.value.uncroppedOriginalBitmap ?: return
        onPhotoCaptured(original)
    }

    fun onConfidenceThresholdChanged(newThreshold: Float) {
        val clampedThreshold = newThreshold.coerceIn(0.10f, 0.95f)
        _uiState.update { it.copy(confidenceThreshold = clampedThreshold) }
        val currentBitmap = _uiState.value.capturedBitmap
        if (currentBitmap != null) {
            analyzeBitmap(currentBitmap, clampedThreshold)
        }
    }

    private fun analyzeBitmap(bitmap: Bitmap, threshold: Float) {
        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null) }

        viewModelScope.launch {
            when (val result = classifyWasteImageUseCase(bitmap, threshold)) {
                is Resource.Success -> {
                    val classification = result.data
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            classificationResult = classification,
                            // If confident, pre-select category, otherwise leave unconfirmed
                            confirmedCategorySlug = if (classification.isConfident) classification.categorySlug else null,
                            confirmedCategoryDisplayName = if (classification.isConfident) classification.categoryDisplayName else null,
                            isManualCategoryPickerOpen = !classification.isConfident
                        )
                    }

                    // If prediction is confident, automatically fetch recommendations
                    if (classification.isConfident && classification.categorySlug != null) {
                        fetchRecommendationsForCategory(classification.categorySlug)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isAnalyzing = false,
                            errorMessage = result.message,
                            isManualCategoryPickerOpen = true
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onCategoryConfirmed(categorySlug: String, categoryDisplayName: String) {
        _uiState.update {
            it.copy(
                confirmedCategorySlug = categorySlug,
                confirmedCategoryDisplayName = categoryDisplayName,
                isManualCategoryPickerOpen = false
            )
        }
        fetchRecommendationsForCategory(categorySlug)
    }

    fun onManualCategoryRequested() {
        _uiState.update { it.copy(isManualCategoryPickerOpen = true) }
    }

    fun onLocationUpdated(lat: Double?, lng: Double?, isGranted: Boolean) {
        _uiState.update {
            it.copy(
                userLat = lat,
                userLng = lng,
                isLocationPermissionGranted = isGranted
            )
        }

        // If recommendations already visible, refresh with newly acquired coordinates
        val currentCategory = _uiState.value.confirmedCategorySlug
        if (currentCategory != null && _uiState.value.hasRecommendationsSearched) {
            fetchRecommendationsForCategory(currentCategory)
        }
    }

    fun onLocalitySelected(locality: String) {
        _uiState.update { it.copy(selectedLocality = locality) }

        val currentCategory = _uiState.value.confirmedCategorySlug
        if (currentCategory != null && _uiState.value.hasRecommendationsSearched) {
            fetchRecommendationsForCategory(currentCategory)
        }
    }

    private fun fetchRecommendationsForCategory(categorySlug: String) {
        _uiState.update { it.copy(isLoadingRecommendations = true) }

        viewModelScope.launch {
            val currentState = _uiState.value
            val result = getRecommendedCentresUseCase(
                categorySlug = categorySlug,
                userLat = currentState.userLat,
                userLng = currentState.userLng,
                fallbackLocality = currentState.selectedLocality
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingRecommendations = false,
                            recommendedPartners = result.data,
                            hasRecommendationsSearched = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingRecommendations = false,
                            errorMessage = result.message,
                            hasRecommendationsSearched = true,
                            recommendedPartners = emptyList()
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onRetakePhoto() {
        _uiState.update {
            it.copy(
                capturedBitmap = null,
                uncroppedOriginalBitmap = null,
                isCropping = false,
                classificationResult = null,
                confirmedCategorySlug = null,
                confirmedCategoryDisplayName = null,
                isManualCategoryPickerOpen = false,
                recommendedPartners = emptyList(),
                hasRecommendationsSearched = false,
                targetPartnerForDropOff = null,
                errorMessage = null
            )
        }
    }

    fun selectPartnerForDropOff(partner: Partner) {
        _uiState.update { it.copy(targetPartnerForDropOff = partner) }
    }

    fun clearTargetPartner() {
        _uiState.update { it.copy(targetPartnerForDropOff = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
