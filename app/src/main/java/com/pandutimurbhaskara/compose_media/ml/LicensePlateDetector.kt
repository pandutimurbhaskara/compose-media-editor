package com.pandutimurbhaskara.compose_media.ml

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.pandutimurbhaskara.compose_media.model.DetectedText
import java.util.UUID

/**
 * Detector for vehicle license plates using ML Kit Text Recognition
 * Supports multiple country formats and validates aspect ratios
 */
class LicensePlateDetector(
    private val textRecognizer: TextRecognitionManager
) {

    /**
     * License plate patterns for various countries
     */
    private val platePatterns = listOf(
        // Indonesia (B 1234 XYZ, D 5678 AB)
        Regex("\\b[A-Z]{1,2}\\s?\\d{1,4}\\s?[A-Z]{1,3}\\b"),

        // USA (ABC 1234, ABC-1234)
        Regex("\\b[A-Z]{3}\\s?-?\\d{3,4}\\b"),

        // Europe (AB-123-CD, AB 123 CD)
        Regex("\\b[A-Z]{2}\\s?-?\\d{2,3}\\s?-?[A-Z]{2}\\b"),

        // UK (AB12 CDE, AB12CDE)
        Regex("\\b[A-Z]{2}\\d{2}\\s?[A-Z]{3}\\b"),

        // Canada (ABC 123, ABC-123)
        Regex("\\b[A-Z]{3}\\s?-?\\d{3}\\b"),

        // Australia (ABC 123, ABC-123)
        Regex("\\b[A-Z]{3}\\s?-?\\d{3}\\b"),

        // Japan (品川 123 あ 45-67)
        Regex("\\b\\d{2,3}\\s?[\\u3040-\\u309F\\u30A0-\\u30FF]\\s?\\d{2}\\s?-?\\d{2}\\b"),

        // China (京A·12345, 粤B·12345)
        Regex("\\b[\\u4E00-\\u9FFF][A-Z]\\s?[·•]?\\s?[A-Z0-9]{5,6}\\b"),

        // Generic alphanumeric (5-8 characters)
        Regex("\\b[A-Z0-9]{5,8}\\b"),

        // Numbers only (common for some countries)
        Regex("\\b\\d{5,7}\\b")
    )

    /**
     * Common plate-related keywords for context validation
     */
    private val plateKeywords = listOf(
        "vehicle", "car", "auto", "truck", "motorcycle", "motor",
        "license", "licence", "plate", "registration", "reg",
        "kendaraan", "mobil", "plat", "nomor"
    )

    /**
     * Minimum and maximum aspect ratios for license plates
     * Most plates are rectangular with width > height
     */
    private val minAspectRatio = 1.8f // Minimum width/height ratio
    private val maxAspectRatio = 5.0f // Maximum width/height ratio

    /**
     * Minimum width for plate detection (to filter out small text)
     */
    private val minPlateWidth = 50 // pixels

    /**
     * Detect license plates in an image
     * @param bitmap The image to process
     * @return List of detected license plate regions
     */
    suspend fun detectLicensePlate(bitmap: Bitmap): List<DetectedText> {
        return try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val recognizedText = textRecognizer.recognizeText(inputImage)

            val detectedPlates = mutableListOf<DetectedText>()

            // Process all text blocks
            recognizedText.textBlocks.forEach { block ->
                block.lines.forEach { line ->
                    val text = line.text.replace("\\s+".toRegex(), " ").trim()

                    // Check if text matches plate pattern
                    if (matchesPlatePattern(text)) {
                        line.boundingBox?.let { box ->
                            // Validate aspect ratio and size
                            if (hasPlateAspectRatio(box) && isLargeEnough(box)) {
                                detectedPlates.add(
                                    DetectedText(
                                        text = text,
                                        boundingBox = box,
                                        confidence = line.confidence ?: 0.8f,
                                        id = UUID.randomUUID().toString()
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Also check elements (individual characters) for plates split across lines
            recognizedText.textBlocks.forEach { block ->
                val blockText = block.text.replace("\\s+".toRegex(), "").trim()

                if (matchesPlatePattern(blockText)) {
                    block.boundingBox?.let { box ->
                        if (hasPlateAspectRatio(box) && isLargeEnough(box)) {
                            // Check if not already detected
                            val notAlreadyDetected = detectedPlates.none { detected ->
                                boxesOverlap(detected.boundingBox, box)
                            }

                            if (notAlreadyDetected) {
                                detectedPlates.add(
                                    DetectedText(
                                        text = blockText,
                                        boundingBox = box,
                                        confidence = block.confidence ?: 0.7f,
                                        id = UUID.randomUUID().toString()
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Remove duplicates and overlapping detections
            removeDuplicates(detectedPlates)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Check if text matches any license plate pattern
     */
    private fun matchesPlatePattern(text: String): Boolean {
        // Clean text for pattern matching
        val cleanText = text.uppercase().trim()

        // Too short or too long
        if (cleanText.length < 4 || cleanText.length > 12) {
            return false
        }

        // Must contain at least one letter or number
        if (!cleanText.any { it.isLetterOrDigit() }) {
            return false
        }

        // Check against patterns
        return platePatterns.any { pattern ->
            pattern.matches(cleanText) || pattern.containsMatchIn(cleanText)
        }
    }

    /**
     * Check if bounding box has typical license plate aspect ratio
     * License plates are typically 2-5 times wider than they are tall
     */
    private fun hasPlateAspectRatio(box: Rect): Boolean {
        if (box.width() <= 0 || box.height() <= 0) return false

        val aspectRatio = box.width().toFloat() / box.height().toFloat()
        return aspectRatio in minAspectRatio..maxAspectRatio
    }

    /**
     * Check if plate is large enough to be a real plate
     * Filters out small text that might match pattern
     */
    private fun isLargeEnough(box: Rect): Boolean {
        return box.width() >= minPlateWidth
    }

    /**
     * Check if two bounding boxes overlap
     */
    private fun boxesOverlap(box1: Rect, box2: Rect): Boolean {
        return box1.intersect(box2)
    }

    /**
     * Calculate overlap percentage between two boxes
     */
    private fun calculateOverlap(box1: Rect, box2: Rect): Float {
        if (!boxesOverlap(box1, box2)) return 0f

        val intersectRect = Rect(box1)
        intersectRect.intersect(box2)

        val intersectArea = intersectRect.width() * intersectRect.height()
        val box1Area = box1.width() * box1.height()
        val box2Area = box2.width() * box2.height()

        val minArea = minOf(box1Area, box2Area)
        return if (minArea > 0) {
            intersectArea.toFloat() / minArea.toFloat()
        } else {
            0f
        }
    }

    /**
     * Remove duplicate and overlapping detections
     * Keep the one with higher confidence
     */
    private fun removeDuplicates(detectedPlates: MutableList<DetectedText>): List<DetectedText> {
        val result = mutableListOf<DetectedText>()

        // Sort by confidence (highest first)
        detectedPlates.sortedByDescending { it.confidence }.forEach { current ->
            val overlapsSignificantly = result.any { existing ->
                calculateOverlap(existing.boundingBox, current.boundingBox) > 0.5f
            }

            if (!overlapsSignificantly) {
                result.add(current)
            }
        }

        return result
    }

    /**
     * Validate plate format for specific country
     * Returns true if text matches expected format for country
     */
    fun validatePlateFormat(text: String, countryCode: String): Boolean {
        val cleanText = text.uppercase().replace("\\s+".toRegex(), "")

        return when (countryCode.uppercase()) {
            "ID", "IDN" -> { // Indonesia
                Regex("[A-Z]{1,2}\\d{1,4}[A-Z]{1,3}").matches(cleanText)
            }
            "US", "USA" -> { // United States
                Regex("[A-Z]{3}\\d{3,4}").matches(cleanText)
            }
            "GB", "UK" -> { // United Kingdom
                Regex("[A-Z]{2}\\d{2}[A-Z]{3}").matches(cleanText)
            }
            "DE", "DEU" -> { // Germany
                Regex("[A-Z]{1,3}-[A-Z]{1,2}\\d{1,4}").matches(cleanText)
            }
            "FR", "FRA" -> { // France
                Regex("[A-Z]{2}-\\d{3}-[A-Z]{2}").matches(cleanText)
            }
            "JP", "JPN" -> { // Japan
                cleanText.length in 7..9 && cleanText.any { it.isDigit() }
            }
            "CN", "CHN" -> { // China
                cleanText.length in 7..8 && cleanText[0].isLetter()
            }
            "AU", "AUS" -> { // Australia
                Regex("[A-Z]{3}\\d{3}").matches(cleanText) || Regex("\\d{3}[A-Z]{3}").matches(cleanText)
            }
            "CA", "CAN" -> { // Canada
                Regex("[A-Z]{3}\\d{3,4}").matches(cleanText)
            }
            else -> {
                // Generic validation - must be alphanumeric, 5-8 chars
                cleanText.length in 5..8 && cleanText.any { it.isLetter() } && cleanText.any { it.isDigit() }
            }
        }
    }

    /**
     * Get country hint from detected plate text
     * Returns likely country code or null if unknown
     */
    fun guessCountryFromPlate(text: String): String? {
        val cleanText = text.uppercase().replace("\\s+".toRegex(), "")

        return when {
            // Indonesia - starts with 1-2 letters, ends with 1-3 letters
            Regex("[A-Z]{1,2}\\d{1,4}[A-Z]{1,3}").matches(cleanText) -> "ID"

            // UK - specific format AB12CDE
            Regex("[A-Z]{2}\\d{2}[A-Z]{3}").matches(cleanText) -> "GB"

            // Germany - has hyphens
            cleanText.contains("-") && Regex("[A-Z]{1,3}-[A-Z]{1,2}\\d{1,4}").matches(cleanText) -> "DE"

            // France - AB-123-CD format
            Regex("[A-Z]{2}-\\d{3}-[A-Z]{2}").matches(cleanText) -> "FR"

            // US/Canada - ABC123 or ABC1234
            Regex("[A-Z]{3}\\d{3,4}").matches(cleanText) -> "US"

            // Contains Chinese/Japanese characters
            cleanText.any { it in '\u4E00'..'\u9FFF' } -> "CN"
            cleanText.any { it in '\u3040'..'\u30FF' } -> "JP"

            else -> null
        }
    }
}
