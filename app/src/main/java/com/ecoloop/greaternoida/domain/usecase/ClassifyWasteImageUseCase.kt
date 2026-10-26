package com.ecoloop.greaternoida.domain.usecase

import android.graphics.Bitmap
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.data.ml.WasteLabelMapper
import com.ecoloop.greaternoida.domain.ml.ImageClassifier
import com.ecoloop.greaternoida.domain.model.AiClassificationResult
import javax.inject.Inject

class ClassifyWasteImageUseCase @Inject constructor(
    private val classifier: ImageClassifier
) {
    val isModelAvailable: Boolean
        get() = classifier.isModelAvailable

    val modelStatusMessage: String
        get() = classifier.modelStatusMessage

    suspend operator fun invoke(
        bitmap: Bitmap,
        threshold: Float = WasteLabelMapper.DEFAULT_CONFIDENCE_THRESHOLD
    ): Resource<AiClassificationResult> {
        return classifier.classify(bitmap, threshold)
    }
}
