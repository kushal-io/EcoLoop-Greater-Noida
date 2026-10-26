package com.ecoloop.greaternoida.data.ml

import com.ecoloop.greaternoida.core.constants.EcoConstants
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WasteLabelMapperTest {

    @Test
    fun mapsPlasticItems_whenConfidenceIsAboveThreshold() {
        val result = WasteLabelMapper.mapLabelToCategory("water bottle", 0.85f)
        assertThat(result.categorySlug).isEqualTo(EcoConstants.WASTE_TYPE_PLASTIC)
        assertThat(result.categoryDisplayName).isEqualTo("Plastic")
        assertThat(result.isConfident).isTrue()
        assertThat(result.requiresManualCorrection).isFalse()
        assertThat(result.confidencePercent).isEqualTo(85)
    }

    @Test
    fun mapsPaperItems_whenConfidenceIsAboveThreshold() {
        val result = WasteLabelMapper.mapLabelToCategory("carton, cardboard box", 0.92f)
        assertThat(result.categorySlug).isEqualTo(EcoConstants.WASTE_TYPE_PAPER)
        assertThat(result.categoryDisplayName).isEqualTo("Paper & Cardboard")
        assertThat(result.isConfident).isTrue()
        assertThat(result.requiresManualCorrection).isFalse()
    }

    @Test
    fun mapsMetalItems_whenConfidenceIsAboveThreshold() {
        val result = WasteLabelMapper.mapLabelToCategory("tin can", 0.78f)
        assertThat(result.categorySlug).isEqualTo(EcoConstants.WASTE_TYPE_METAL)
        assertThat(result.categoryDisplayName).isEqualTo("Metal")
        assertThat(result.isConfident).isTrue()
        assertThat(result.requiresManualCorrection).isFalse()
    }

    @Test
    fun mapsGlassItems_whenConfidenceIsAboveThreshold() {
        val result = WasteLabelMapper.mapLabelToCategory("wine bottle", 0.81f)
        assertThat(result.categorySlug).isEqualTo(EcoConstants.WASTE_TYPE_GLASS)
        assertThat(result.categoryDisplayName).isEqualTo("Glass")
        assertThat(result.isConfident).isTrue()
        assertThat(result.requiresManualCorrection).isFalse()
    }

    @Test
    fun mapsEWasteItems_whenConfidenceIsAboveThreshold() {
        val result = WasteLabelMapper.mapLabelToCategory("cellular telephone", 0.89f)
        assertThat(result.categorySlug).isEqualTo(EcoConstants.WASTE_TYPE_E_WASTE)
        assertThat(result.categoryDisplayName).isEqualTo("E-Waste")
        assertThat(result.isConfident).isTrue()
        assertThat(result.requiresManualCorrection).isFalse()
    }

    @Test
    fun flagsLowConfidence_whenConfidenceIsBelowThreshold() {
        val result = WasteLabelMapper.mapLabelToCategory("water bottle", 0.35f, threshold = 0.50f)
        assertThat(result.categorySlug).isEqualTo(EcoConstants.WASTE_TYPE_PLASTIC)
        assertThat(result.isConfident).isFalse()
        assertThat(result.requiresManualCorrection).isTrue()
        assertThat(result.confidencePercent).isEqualTo(35)
    }

    @Test
    fun handlesLowConfidenceJoystickPrediction_safelyWithoutAutoConfirming() {
        // Reproduces user scenario: 19% joystick prediction
        val result = WasteLabelMapper.mapLabelToCategory("joystick", 0.19f, threshold = 0.50f)
        assertThat(result.confidencePercent).isEqualTo(19)
        assertThat(result.isConfident).isFalse()
        assertThat(result.requiresManualCorrection).isTrue()
        assertThat(result.explanationMessage).contains("below the 50% threshold")
    }

    @Test
    fun detectsMultiObjectClutter_whenCandidatesAreDispersed() {
        // Multi-object scene: mouse, cables, laptop producing low-confidence candidate split
        val candidates = listOf(
            Pair("joystick", 0.19f),
            Pair("mouse", 0.16f),
            Pair("notebook computer", 0.14f)
        )
        val result = WasteLabelMapper.mapPredictions(candidates, threshold = 0.50f)

        assertThat(result.isConfident).isFalse()
        assertThat(result.requiresManualCorrection).isTrue()
        assertThat(result.isMultiObjectOrCluttered).isTrue()
        assertThat(result.explanationMessage).contains("Multiple items or cluttered background detected")
        assertThat(result.topCandidates).hasSize(3)
        assertThat(result.topCandidates[0].confidencePercent).isEqualTo(19)
        assertThat(result.topCandidates[1].confidencePercent).isEqualTo(16)
        assertThat(result.topCandidates[2].confidencePercent).isEqualTo(14)
    }

    @Test
    fun detectsConflictingCategories_inMultiObjectPredictions() {
        // Conflicting items: metal tin can and plastic water bottle in frame
        val candidates = listOf(
            Pair("tin can", 0.32f),
            Pair("water bottle", 0.28f)
        )
        val result = WasteLabelMapper.mapPredictions(candidates, threshold = 0.50f)

        assertThat(result.isMultiObjectOrCluttered).isTrue()
        assertThat(result.isConfident).isFalse()
        assertThat(result.requiresManualCorrection).isTrue()
    }

    @Test
    fun flagsUnrecognizedItem_whenLabelDoesNotMatchWasteKeywords() {
        val result = WasteLabelMapper.mapLabelToCategory("African elephant", 0.95f)
        assertThat(result.categorySlug).isNull()
        assertThat(result.categoryDisplayName).isEqualTo("Unrecognized Waste")
        assertThat(result.isConfident).isFalse()
        assertThat(result.requiresManualCorrection).isTrue()
        assertThat(result.explanationMessage).contains("not recognized as a supported recyclable material")
    }

    @Test
    fun flagsUnsupportedFurnitureItem_asUnrecognized() {
        val result = WasteLabelMapper.mapLabelToCategory("dining table", 0.88f)
        assertThat(result.categorySlug).isNull()
        assertThat(result.isConfident).isFalse()
        assertThat(result.requiresManualCorrection).isTrue()
        assertThat(result.explanationMessage).contains("dining table")
    }

    @Test
    fun flagsEmptyOrBlankLabel_asUnrecognized() {
        val result = WasteLabelMapper.mapLabelToCategory("", 0.0f)
        assertThat(result.categorySlug).isNull()
        assertThat(result.isConfident).isFalse()
        assertThat(result.requiresManualCorrection).isTrue()
    }

    @Test
    fun handlesInvalidScoresSafely() {
        // Test NaN
        val nanResult = WasteLabelMapper.mapLabelToCategory("mouse", Float.NaN)
        assertThat(nanResult.confidence).isEqualTo(0f)
        assertThat(nanResult.isConfident).isFalse()

        // Test Negative
        val negResult = WasteLabelMapper.mapLabelToCategory("mouse", -0.5f)
        assertThat(negResult.confidence).isEqualTo(0f)

        // Test Overflow
        val overResult = WasteLabelMapper.mapLabelToCategory("mouse", 1.85f, threshold = 0.50f)
        assertThat(overResult.confidence).isEqualTo(1.0f)
        assertThat(overResult.isConfident).isTrue()

        // Test Empty candidates list
        val emptyResult = WasteLabelMapper.mapPredictions(emptyList())
        assertThat(emptyResult.categorySlug).isNull()
        assertThat(emptyResult.isConfident).isFalse()
    }

    @Test
    fun respectsCustomConfidenceThreshold() {
        // 45% confidence under 40% threshold -> Confident
        val relaxedResult = WasteLabelMapper.mapLabelToCategory("mouse", 0.45f, threshold = 0.40f)
        assertThat(relaxedResult.isConfident).isTrue()
        assertThat(relaxedResult.requiresManualCorrection).isFalse()

        // 45% confidence under 65% threshold -> Not Confident
        val strictResult = WasteLabelMapper.mapLabelToCategory("mouse", 0.45f, threshold = 0.65f)
        assertThat(strictResult.isConfident).isFalse()
        assertThat(strictResult.requiresManualCorrection).isTrue()
    }
}
