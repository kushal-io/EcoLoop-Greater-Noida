package com.ecoloop.greaternoida.ui.aiscanner

import android.graphics.Bitmap
import com.ecoloop.greaternoida.data.ml.WasteLabelMapper
import com.ecoloop.greaternoida.domain.model.AiClassificationResult
import com.ecoloop.greaternoida.domain.model.Partner

data class AiScannerUiState(
    val isModelAvailable: Boolean = true,
    val modelStatusMessage: String = "",
    val isAnalyzing: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val uncroppedOriginalBitmap: Bitmap? = null,
    val isCropping: Boolean = false,
    val confidenceThreshold: Float = WasteLabelMapper.DEFAULT_CONFIDENCE_THRESHOLD,
    val classificationResult: AiClassificationResult? = null,
    val confirmedCategorySlug: String? = null,
    val confirmedCategoryDisplayName: String? = null,
    val isManualCategoryPickerOpen: Boolean = false,
    val isLoadingRecommendations: Boolean = false,
    val recommendedPartners: List<Partner> = emptyList(),
    val hasRecommendationsSearched: Boolean = false,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val isLocationPermissionGranted: Boolean = false,
    val selectedLocality: String = "Pari Chowk",
    val errorMessage: String? = null,
    val targetPartnerForDropOff: Partner? = null
) {
    val isInCaptureMode: Boolean
        get() = capturedBitmap == null

    val isCategoryConfirmed: Boolean
        get() = confirmedCategorySlug != null

    val isRecommendationVisible: Boolean
        get() = isCategoryConfirmed && hasRecommendationsSearched
}
