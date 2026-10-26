package com.ecoloop.greaternoida.domain.model

data class CandidatePrediction(
    val rawLabel: String,
    val confidence: Float,
    val categorySlug: String?
) {
    val confidencePercent: Int
        get() = (confidence * 100).toInt().coerceIn(0, 100)
}

data class AiClassificationResult(
    val rawLabel: String = "",
    val categorySlug: String? = null,
    val categoryDisplayName: String = "Unrecognized Waste",
    val confidence: Float = 0f,
    val isConfident: Boolean = false,
    val requiresManualCorrection: Boolean = true,
    val topCandidates: List<CandidatePrediction> = emptyList(),
    val isMultiObjectOrCluttered: Boolean = false,
    val explanationMessage: String? = null,
    val confidenceThreshold: Float = 0.50f
) {
    val confidencePercent: Int
        get() = (confidence * 100).toInt().coerceIn(0, 100)
}
