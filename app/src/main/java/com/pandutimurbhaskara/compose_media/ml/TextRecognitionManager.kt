package com.pandutimurbhaskara.compose_media.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.pandutimurbhaskara.compose_media.model.DetectedText
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Manager for ML Kit Text Recognition
 * Uses on-device model for fast, offline text detection
 */
class TextRecognitionManager {

    private val recognizer: TextRecognizer

    init {
        // Use default Latin text recognizer (supports English and other Latin scripts)
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Recognize text in a bitmap image
     * @param bitmap The image to process
     * @return ML Kit Text object containing all detected text
     */
    suspend fun recognizeText(bitmap: Bitmap): Text {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return recognizer.process(inputImage).await()
    }

    /**
     * Recognize text and convert to DetectedText list
     * @param bitmap The image to process
     * @return List of detected text blocks with bounding boxes
     */
    suspend fun recognizeTextBlocks(bitmap: Bitmap): List<DetectedText> {
        return try {
            val text = recognizeText(bitmap)
            val detectedTexts = mutableListOf<DetectedText>()

            // Process each text block
            text.textBlocks.forEach { block ->
                block.lines.forEach { line ->
                    line.boundingBox?.let { box ->
                        detectedTexts.add(
                            DetectedText(
                                text = line.text,
                                boundingBox = box,
                                confidence = line.confidence ?: 0f,
                                id = UUID.randomUUID().toString()
                            )
                        )
                    }
                }
            }

            detectedTexts
        } catch (e: Exception) {
            // Log error and return empty list
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Release recognizer resources
     * Call this when recognizer is no longer needed
     */
    fun release() {
        recognizer.close()
    }
}
