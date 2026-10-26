package com.ecoloop.greaternoida.data.ml

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.ecoloop.greaternoida.domain.model.AiClassificationResult
import com.ecoloop.greaternoida.domain.model.CandidatePrediction

object WasteLabelMapper {

    const val DEFAULT_CONFIDENCE_THRESHOLD = 0.50f

    // Specific glass items (prioritized so glass bottles are not misclassified as plastic)
    private val glassKeywords = setOf(
        "wine bottle", "beer bottle", "beer glass", "goblet", "vase",
        "jar", "glass", "cullet", "chalice", "cocktail shaker", "flute"
    )

    private val eWasteKeywords = setOf(
        "cellular telephone", "desktop computer", "computer keyboard", "monitor",
        "hand-held computer", "notebook computer", "laptop", "television", "hard disc",
        "modem", "printer", "tape player", "loudspeaker", "remote control", "joystick",
        "ipod", "reflex camera", "Polaroid camera", "radio", "hair dryer", "toaster", "microwave",
        "vacuum", "battery", "charger", "mouse", "screen", "dial telephone", "pay-phone",
        "cassette player", "CD player", "electric fan", "space heater", "refrigerator",
        "microphone"
    )

    private val metalKeywords = setOf(
        "tin can", "beer can", "pop can", "milk can", "steel drum", "safe",
        "pot", "frying pan", "brass", "iron", "wrench", "hammer", "nail",
        "padlock", "aluminum", "copper", "wire", "ashcan", "can opener"
    )

    private val plasticKeywords = setOf(
        "water bottle", "pop bottle", "pill bottle", "plastic bag", "sunscreen",
        "measuring cup", "tub", "water jug", "milk jug", "lotion", "detergent",
        "plastic", "polythene", "straw", "plastic bottle"
    )

    private val paperKeywords = setOf(
        "carton", "packet", "paper towel", "notebook", "envelope", "comic book",
        "book jacket", "menu", "toilet tissue", "cardboard", "box", "newspaper",
        "binder", "pencil box", "paper", "brochure", "pamphlet"
    )

    /**
     * Checks if a label contains the keyword as a whole word or phrase, avoiding partial word mismatches
     * (e.g. avoiding "can" matching inside "African").
     */
    private fun matchesKeyword(label: String, keyword: String): Boolean {
        val regex = Regex("""\b${Regex.escape(keyword)}\b""", RegexOption.IGNORE_CASE)
        return regex.containsMatchIn(label)
    }

    /**
     * Resolves a raw label string into one of our 5 waste category slugs, or null if unsupported.
     */
    fun determineCategory(rawLabel: String): String? {
        val cleanLabel = rawLabel.trim()
        if (cleanLabel.isBlank()) return null

        return when {
            glassKeywords.any { matchesKeyword(cleanLabel, it) } -> EcoConstants.WASTE_TYPE_GLASS
            eWasteKeywords.any { matchesKeyword(cleanLabel, it) } -> EcoConstants.WASTE_TYPE_E_WASTE
            metalKeywords.any { matchesKeyword(cleanLabel, it) } -> EcoConstants.WASTE_TYPE_METAL
            plasticKeywords.any { matchesKeyword(cleanLabel, it) } -> EcoConstants.WASTE_TYPE_PLASTIC
            paperKeywords.any { matchesKeyword(cleanLabel, it) } -> EcoConstants.WASTE_TYPE_PAPER
            else -> null
        }
    }

    fun getDisplayName(categorySlug: String?): String {
        return when (categorySlug) {
            EcoConstants.WASTE_TYPE_PLASTIC -> "Plastic"
            EcoConstants.WASTE_TYPE_PAPER -> "Paper & Cardboard"
            EcoConstants.WASTE_TYPE_METAL -> "Metal"
            EcoConstants.WASTE_TYPE_GLASS -> "Glass"
            EcoConstants.WASTE_TYPE_E_WASTE -> "E-Waste"
            else -> "Unrecognized Waste"
        }
    }

    /**
     * Maps a single on-device prediction label and confidence score.
     */
    fun mapLabelToCategory(
        rawLabel: String,
        confidence: Float,
        threshold: Float = DEFAULT_CONFIDENCE_THRESHOLD
    ): AiClassificationResult {
        return mapPredictions(
            topCandidates = listOf(Pair(rawLabel, confidence)),
            threshold = threshold
        )
    }

    /**
     * Maps multiple candidate predictions (Top-K) into a unified domain result,
     * evaluating confidence thresholding, multi-object clutter, and unsupported labels.
     */
    fun mapPredictions(
        topCandidates: List<Pair<String, Float>>,
        threshold: Float = DEFAULT_CONFIDENCE_THRESHOLD
    ): AiClassificationResult {
        if (topCandidates.isEmpty()) {
            return AiClassificationResult(
                rawLabel = "",
                categorySlug = null,
                categoryDisplayName = "Unrecognized Waste",
                confidence = 0f,
                isConfident = false,
                requiresManualCorrection = true,
                explanationMessage = "No classification predictions received from model.",
                confidenceThreshold = threshold
            )
        }

        // Process all candidates
        val candidatePredictions = topCandidates.map { (label, score) ->
            val cleanScore = if (score.isNaN()) 0f else score.coerceIn(0f, 1f)
            val category = determineCategory(label)
            CandidatePrediction(
                rawLabel = label.trim(),
                confidence = cleanScore,
                categorySlug = category
            )
        }

        val top = candidatePredictions.first()
        val topCategory = top.categorySlug
        val topConfidence = top.confidence
        val topDisplayName = getDisplayName(topCategory)

        // Determine if prediction meets the threshold
        val meetsThreshold = topCategory != null && topConfidence >= threshold

        // Multi-object / Clutter detection:
        // Triggered when:
        // 1. Top confidence is low (< 0.40f) while multiple candidates exist, OR
        // 2. The top candidates span 2 or more distinct non-null categories (e.g. e-waste and metal/plastic)
        val distinctCategories = candidatePredictions.mapNotNull { it.categorySlug }.distinct()
        val isLowConfidenceSplit = topConfidence < 0.40f && candidatePredictions.size > 1
        val hasConflictingCategories = distinctCategories.size >= 2

        val isMultiObjectOrCluttered = !meetsThreshold && (isLowConfidenceSplit || hasConflictingCategories)

        val explanationMessage: String? = when {
            top.rawLabel.isBlank() -> {
                "Empty prediction output from model."
            }
            topCategory == null -> {
                "The detected item ('${top.rawLabel}') is not recognized as a supported recyclable material. Please crop to isolate the item or select the category manually."
            }
            isMultiObjectOrCluttered -> {
                val candidateSummary = candidatePredictions.take(3).joinToString {
                    "${it.rawLabel} (${it.confidencePercent}%)"
                }
                "Multiple items or cluttered background detected ($candidateSummary). Single-item classifiers cannot reliably determine waste in cluttered scenes. Use 'Crop & Focus' to isolate one item, or select the category below."
            }
            !meetsThreshold -> {
                "Confidence is ${(topConfidence * 100).toInt()}% (below the ${(threshold * 100).toInt()}% threshold). Detected possible '${top.rawLabel}'. Please verify or select the category manually."
            }
            else -> null
        }

        return AiClassificationResult(
            rawLabel = top.rawLabel,
            categorySlug = topCategory,
            categoryDisplayName = topDisplayName,
            confidence = topConfidence,
            isConfident = meetsThreshold,
            requiresManualCorrection = !meetsThreshold,
            topCandidates = candidatePredictions,
            isMultiObjectOrCluttered = isMultiObjectOrCluttered,
            explanationMessage = explanationMessage,
            confidenceThreshold = threshold
        )
    }
}
