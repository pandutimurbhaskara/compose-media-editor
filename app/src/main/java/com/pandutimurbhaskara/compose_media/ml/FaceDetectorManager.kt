package com.pandutimurbhaskara.compose_media.ml

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.pandutimurbhaskara.compose_media.model.DetectedFace
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Manager for ML Kit Face Detection
 * Uses on-device model for fast, offline face detection
 */
class FaceDetectorManager {

    private val detector: FaceDetector

    init {
        // Configure face detector for accuracy and performance
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f) // Minimum face size relative to image (15%)
            .enableTracking() // Enable tracking for video processing
            .build()

        detector = FaceDetection.getClient(options)
    }

    /**
     * Detect faces in a bitmap image
     * @param bitmap The image to process
     * @return List of detected faces with bounding boxes
     */
    suspend fun detectFaces(bitmap: Bitmap): List<DetectedFace> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)

            // Process image with ML Kit
            val faces: List<Face> = detector.process(inputImage).await()

            // Convert ML Kit Face objects to our DetectedFace model
            faces.map { face ->
                DetectedFace(
                    boundingBox = face.boundingBox,
                    id = UUID.randomUUID().toString()
                )
            }
        } catch (e: Exception) {
            // Log error and return empty list
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Release detector resources
     * Call this when detector is no longer needed
     */
    fun release() {
        detector.close()
    }
}
