package com.pandutimurbhaskara.compose_media.model

import android.graphics.Rect
import java.util.UUID

/**
 * Sealed class for detection results
 */
sealed class DetectionResult {
    data class FaceDetection(
        val faces: List<DetectedFace>
    ) : DetectionResult()

    data class TextDetection(
        val textBlocks: List<DetectedText>
    ) : DetectionResult()
}

/**
 * Detected face with bounding box
 */
data class DetectedFace(
    val boundingBox: Rect,
    val id: String = UUID.randomUUID().toString()
)

/**
 * Detected text with bounding box and metadata
 */
data class DetectedText(
    val text: String,
    val boundingBox: Rect,
    val confidence: Float,
    val id: String = UUID.randomUUID().toString()
)

/**
 * Blur region with type and source information
 */
data class BlurRegion(
    val id: String,
    val boundingBox: Rect,
    val type: BlurType,
    val source: DetectionSource
)

/**
 * Types of blur effects
 */
enum class BlurType {
    GAUSSIAN,      // Smooth Gaussian blur
    PIXELATION,    // Pixelated/mosaic effect
    BLACK_BOX      // Solid black rectangle
}

/**
 * Source of blur region detection
 */
enum class DetectionSource {
    AUTO_FACE,           // Automatically detected face
    AUTO_ID_CARD,        // Automatically detected ID card
    AUTO_LICENSE_PLATE,  // Automatically detected license plate
    MANUAL               // Manually added by user
}
