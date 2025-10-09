package com.pandutimurbhaskara.compose_media.ml

import android.graphics.Bitmap
import android.graphics.Rect
import com.pandutimurbhaskara.compose_media.model.DetectedText
import java.util.UUID

/**
 * Detector for ID card information using ML Kit Text Recognition
 * Detects and identifies sensitive information like ID numbers and names
 */
class IdCardDetector(
    private val textRecognizer: TextRecognitionManager
) {

    /**
     * Patterns for ID numbers from various countries/formats
     */
    private val idNumberPatterns = listOf(
        // Indonesian NIK (16 digits)
        Regex("\\b\\d{16}\\b"),

        // Passport numbers (1-2 letters followed by 6-9 digits)
        Regex("\\b[A-Z]{1,2}\\d{6,9}\\b"),

        // Driver's license (8-12 alphanumeric)
        Regex("\\b[A-Z0-9]{8,12}\\b"),

        // Social Security Number format (XXX-XX-XXXX)
        Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"),

        // Generic ID numbers (6-16 digits)
        Regex("\\b\\d{6,16}\\b"),

        // Alphanumeric IDs with common patterns
        Regex("\\b[A-Z]{2}\\d{6,8}\\b"),
        Regex("\\b\\d{2}[A-Z]{2}\\d{4,6}\\b")
    )

    /**
     * Common labels for names on IDs in multiple languages
     */
    private val nameLabels = listOf(
        // English
        "name", "full name", "given name", "surname", "first name", "last name",

        // Indonesian
        "nama", "nama lengkap",

        // Spanish
        "nombre", "apellido",

        // French
        "nom", "prénom",

        // German
        "vorname", "nachname",

        // Portuguese
        "nome",

        // Generic
        "holder", "bearer"
    )

    /**
     * Common ID card keywords
     */
    private val idCardKeywords = listOf(
        "id", "card", "identity", "identification", "passport", "license", "licence",
        "ktp", "sim", "driving", "national", "citizen", "resident"
    )

    /**
     * Date patterns (often birth dates on IDs)
     */
    private val datePatterns = listOf(
        Regex("\\b\\d{2}/\\d{2}/\\d{4}\\b"),
        Regex("\\b\\d{2}-\\d{2}-\\d{4}\\b"),
        Regex("\\b\\d{4}/\\d{2}/\\d{2}\\b"),
        Regex("\\b\\d{4}-\\d{2}-\\d{2}\\b")
    )

    /**
     * Detect ID card information in an image
     * @param bitmap The image to process
     * @return List of detected text regions containing sensitive information
     */
    suspend fun detectIdCard(bitmap: Bitmap): List<DetectedText> {
        return try {
            val recognizedText = textRecognizer.recognizeText(bitmap)

            val detectedTexts = mutableListOf<DetectedText>()
            val allTextBlocks = mutableListOf<String>()

            // First pass: collect all text to check if this looks like an ID card
            recognizedText.textBlocks.forEach { block ->
                allTextBlocks.add(block.text.lowercase())
            }

            val isLikelyIdCard = allTextBlocks.any { text ->
                idCardKeywords.any { keyword -> text.contains(keyword) }
            }

            // Process text blocks
            recognizedText.textBlocks.forEach { block ->
                block.lines.forEach { line ->
                    val text = line.text.trim()
                    val cleanText = text.replace("\\s+".toRegex(), " ")

                    // Check if line matches ID number pattern
                    if (matchesIdPattern(cleanText)) {
                        line.boundingBox?.let { box ->
                            detectedTexts.add(
                                DetectedText(
                                    text = cleanText,
                                    boundingBox = box,
                                    confidence = line.confidence ?: 0.8f,
                                    id = UUID.randomUUID().toString()
                                )
                            )
                        }
                    }

                    // Check if line matches date pattern (likely birth date)
                    if (matchesDatePattern(cleanText) && isLikelyIdCard) {
                        line.boundingBox?.let { box ->
                            detectedTexts.add(
                                DetectedText(
                                    text = cleanText,
                                    boundingBox = expandBox(box, 10, 5),
                                    confidence = line.confidence ?: 0.7f,
                                    id = UUID.randomUUID().toString()
                                )
                            )
                        }
                    }

                    // Check if line contains name label
                    if (containsNameLabel(cleanText)) {
                        line.boundingBox?.let { box ->
                            // Expand box to include the actual name (usually on same or next line)
                            val expandedBox = expandBox(box, 20, 60)
                            detectedTexts.add(
                                DetectedText(
                                    text = "Name field",
                                    boundingBox = expandedBox,
                                    confidence = line.confidence ?: 0.7f,
                                    id = UUID.randomUUID().toString()
                                )
                            )
                        }
                    }

                    // Detect sequences of capital letters (likely names)
                    if (isLikelyName(cleanText) && isLikelyIdCard && cleanText.length > 3) {
                        line.boundingBox?.let { box ->
                            // Only add if not already detected
                            val notAlreadyDetected = detectedTexts.none { detected ->
                                boxesOverlap(detected.boundingBox, box)
                            }
                            if (notAlreadyDetected) {
                                detectedTexts.add(
                                    DetectedText(
                                        text = cleanText,
                                        boundingBox = box,
                                        confidence = line.confidence ?: 0.6f,
                                        id = UUID.randomUUID().toString()
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Remove duplicates and overlapping regions
            removeDuplicates(detectedTexts)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Check if text matches any ID number pattern
     */
    private fun matchesIdPattern(text: String): Boolean {
        return idNumberPatterns.any { pattern ->
            pattern.containsMatchIn(text)
        }
    }

    /**
     * Check if text matches date pattern
     */
    private fun matchesDatePattern(text: String): Boolean {
        return datePatterns.any { pattern ->
            pattern.matches(text)
        }
    }

    /**
     * Check if text contains a name label
     */
    private fun containsNameLabel(text: String): Boolean {
        val lowerText = text.lowercase()
        return nameLabels.any { label ->
            lowerText.contains(label)
        }
    }

    /**
     * Check if text is likely a person's name
     * Names are typically 2-4 words of capitalized text
     */
    private fun isLikelyName(text: String): Boolean {
        // Check if text is mostly uppercase or title case
        val words = text.split("\\s+".toRegex())

        if (words.size < 2 || words.size > 4) return false

        // Check if each word starts with capital letter
        val capitalizedWords = words.count { word ->
            word.isNotEmpty() && word[0].isUpperCase() && word.length > 1
        }

        return capitalizedWords >= 2
    }

    /**
     * Expand bounding box by given margins
     */
    private fun expandBox(box: Rect, horizontal: Int, vertical: Int): Rect {
        return Rect(
            (box.left - horizontal).coerceAtLeast(0),
            (box.top - vertical).coerceAtLeast(0),
            box.right + horizontal,
            box.bottom + vertical
        )
    }

    /**
     * Check if two bounding boxes overlap
     */
    private fun boxesOverlap(box1: Rect, box2: Rect): Boolean {
        return box1.intersect(box2)
    }

    /**
     * Remove duplicate and overlapping detections
     */
    private fun removeDuplicates(detectedTexts: MutableList<DetectedText>): List<DetectedText> {
        val result = mutableListOf<DetectedText>()

        detectedTexts.sortedByDescending { it.confidence }.forEach { current ->
            val overlapsWithExisting = result.any { existing ->
                boxesOverlap(existing.boundingBox, current.boundingBox)
            }

            if (!overlapsWithExisting) {
                result.add(current)
            }
        }

        return result
    }
}
