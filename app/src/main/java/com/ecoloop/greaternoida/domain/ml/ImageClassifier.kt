package com.ecoloop.greaternoida.domain.ml

import android.graphics.Bitmap
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.data.ml.WasteLabelMapper
import com.ecoloop.greaternoida.domain.model.AiClassificationResult

interface ImageClassifier {
    /**
     * True if the on-device LiteRT/TensorFlow Lite model file and labels were successfully loaded into memory.
     */
    val isModelAvailable: Boolean

    /**
     * Informative status message explaining model availability (e.g. model name, licence, or instructions to supply it).
     */
    val modelStatusMessage: String

    /**
     * Runs local inference on the provided bitmap using LiteRT/TensorFlow Lite.
     * The input bitmap is processed purely in volatile memory and is never uploaded or persisted to disk.
     */
    suspend fun classify(
        bitmap: Bitmap,
        threshold: Float = WasteLabelMapper.DEFAULT_CONFIDENCE_THRESHOLD
    ): Resource<AiClassificationResult>

    /**
     * Releases native LiteRT interpreter and memory resources.
     */
    fun close()
}
