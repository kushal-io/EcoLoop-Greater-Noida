package com.ecoloop.greaternoida.data.ml

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.ml.ImageClassifier
import com.ecoloop.greaternoida.domain.model.AiClassificationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TfLiteWasteClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageClassifier {

    companion object {
        const val MODEL_FILENAME = "mobilenet_v1_1.0_224_quant.tflite"
        const val LABELS_FILENAME = "labels_mobilenet_quant_v1_224.txt"
        const val INPUT_SIZE = 224
        const val PIXEL_CHANNELS = 3 // R, G, B
        const val OUTPUT_CLASSES = 1001
        const val MODEL_LICENCE = "Apache License 2.0 (Google TensorFlow / MobileNet)"
    }

    private var interpreter: Interpreter? = null
    private val labels = mutableListOf<String>()

    override var isModelAvailable: Boolean = false
        private set

    override var modelStatusMessage: String = "Initializing on-device LiteRT classifier..."
        private set

    init {
        loadModelAndLabels()
    }

    private fun loadModelAndLabels() {
        try {
            // 1. Load labels
            labels.clear()
            context.assets.open(LABELS_FILENAME).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        labels.add(line.trim())
                        line = reader.readLine()
                    }
                }
            }

            if (labels.isEmpty()) {
                isModelAvailable = false
                modelStatusMessage = "Classification labels file ($LABELS_FILENAME) is empty."
                return
            }

            // 2. Load TFLite Model Buffer
            val fileDescriptor: AssetFileDescriptor = context.assets.openFd(MODEL_FILENAME)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            // 3. Initialize Interpreter with multi-threading
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseXNNPACK(true)
            }
            interpreter = Interpreter(modelBuffer, options)

            isModelAvailable = true
            modelStatusMessage = "On-device LiteRT MobileNet v1 quantized loaded successfully ($MODEL_LICENCE)."
        } catch (e: Exception) {
            isModelAvailable = false
            modelStatusMessage = "Failed to load on-device model '$MODEL_FILENAME': ${e.localizedMessage}. " +
                "Ensure the model and labels are placed in app/src/main/assets/."
        }
    }

    override suspend fun classify(
        bitmap: Bitmap,
        threshold: Float
    ): Resource<AiClassificationResult> = withContext(Dispatchers.Default) {
        val localInterpreter = interpreter
        if (!isModelAvailable || localInterpreter == null || labels.isEmpty()) {
            return@withContext Resource.Error(modelStatusMessage)
        }

        try {
            // 1. Center crop to square to preserve aspect ratio (avoids stretching distortion)
            val squareBitmap = if (bitmap.width == bitmap.height) {
                bitmap
            } else {
                val size = minOf(bitmap.width, bitmap.height)
                val x = (bitmap.width - size) / 2
                val y = (bitmap.height - size) / 2
                Bitmap.createBitmap(bitmap, x, y, size, size)
            }

            val scaledBitmap = if (squareBitmap.width == INPUT_SIZE && squareBitmap.height == INPUT_SIZE) {
                squareBitmap
            } else {
                Bitmap.createScaledBitmap(squareBitmap, INPUT_SIZE, INPUT_SIZE, true)
            }

            // 2. Allocate direct ByteBuffer for uint8 input: [1, 224, 224, 3]
            val inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * PIXEL_CHANNELS)
            inputBuffer.order(ByteOrder.nativeOrder())
            inputBuffer.rewind()

            val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
            scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

            // Fill RGB bytes (0-255 uint8)
            var pixelIndex = 0
            for (i in 0 until INPUT_SIZE) {
                for (j in 0 until INPUT_SIZE) {
                    val pixel = intValues[pixelIndex++]
                    // Extract Red, Green, Blue
                    inputBuffer.put(((pixel shr 16) and 0xFF).toByte())
                    inputBuffer.put(((pixel shr 8) and 0xFF).toByte())
                    inputBuffer.put((pixel and 0xFF).toByte())
                }
            }

            // 3. Allocate output array: [1, 1001] uint8
            val outputBuffer = Array(1) { ByteArray(OUTPUT_CLASSES) }

            // 4. Run on-device inference (offline, zero cloud dependencies)
            localInterpreter.run(inputBuffer, outputBuffer)

            // 5. Extract top 5 predictions by score
            val rawScores = outputBuffer[0]
            val top5Indices = IntArray(5) { -1 }
            val top5Scores = IntArray(5) { -1 }

            for (i in rawScores.indices) {
                val unsignedScore = rawScores[i].toInt() and 0xFF
                for (k in 0 until 5) {
                    if (unsignedScore > top5Scores[k]) {
                        for (j in 4 downTo k + 1) {
                            top5Scores[j] = top5Scores[j - 1]
                            top5Indices[j] = top5Indices[j - 1]
                        }
                        top5Scores[k] = unsignedScore
                        top5Indices[k] = i
                        break
                    }
                }
            }

            val topCandidates = (0 until 5)
                .filter { top5Indices[it] >= 0 }
                .map { k ->
                    val idx = top5Indices[k]
                    val label = if (idx in labels.indices) labels[idx] else "class_$idx"
                    val score = (top5Scores[k] / 255.0f).coerceIn(0.0f, 1.0f)
                    Pair(label, score)
                }

            // 6. Map to waste categories with multi-class and clutter analysis
            val result = WasteLabelMapper.mapPredictions(topCandidates, threshold)

            // Clean up temporary bitmaps if created
            if (squareBitmap != bitmap && !squareBitmap.isRecycled) {
                squareBitmap.recycle()
            }
            if (scaledBitmap != bitmap && scaledBitmap != squareBitmap && !scaledBitmap.isRecycled) {
                scaledBitmap.recycle()
            }

            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error("Classification error: ${e.localizedMessage}", e)
        }
    }

    override fun close() {
        try {
            interpreter?.close()
            interpreter = null
        } catch (_: Exception) {}
    }
}
