package com.pandutimurbhaskara.compose_media.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandutimurbhaskara.compose_media.ml.LicensePlateDetector
import com.pandutimurbhaskara.compose_media.ml.TextRecognitionManager
import com.pandutimurbhaskara.compose_media.model.BlurRegion
import com.pandutimurbhaskara.compose_media.model.BlurType
import com.pandutimurbhaskara.compose_media.model.DetectedText
import com.pandutimurbhaskara.compose_media.model.DetectionSource
import com.pandutimurbhaskara.compose_media.util.BlurProcessor
import com.pandutimurbhaskara.compose_media.util.ImageProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing license plate blur editing state
 */
class LicensePlateBlurViewModel(
    private val textRecognitionManager: TextRecognitionManager = TextRecognitionManager(),
    private val licensePlateDetector: LicensePlateDetector = LicensePlateDetector(textRecognitionManager),
    private val blurProcessor: BlurProcessor = BlurProcessor(),
    private val imageProcessor: ImageProcessor? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<LicensePlateBlurUiState>(LicensePlateBlurUiState.Idle)
    val uiState: StateFlow<LicensePlateBlurUiState> = _uiState.asStateFlow()

    private var originalBitmap: Bitmap? = null
    private val _detectedPlates = mutableListOf<DetectedText>()
    private val _blurRegions = mutableListOf<BlurRegion>()

    /**
     * Load image from URI and prepare for editing
     */
    fun loadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = LicensePlateBlurUiState.Loading

                val processor = imageProcessor ?: ImageProcessor(context)
                originalBitmap = processor.loadAndOptimizeImage(uri)

                _uiState.value = LicensePlateBlurUiState.ImageLoaded(originalBitmap!!)
            } catch (e: Exception) {
                _uiState.value = LicensePlateBlurUiState.Error(e.message ?: "Failed to load image")
            }
        }
    }

    /**
     * Detect license plates in the loaded image
     */
    fun detectLicensePlates() {
        viewModelScope.launch {
            try {
                val bitmap = originalBitmap ?: return@launch
                _uiState.value = LicensePlateBlurUiState.Detecting

                _detectedPlates.clear()
                _detectedPlates.addAll(licensePlateDetector.detectLicensePlate(bitmap))

                // Convert detected plates to blur regions
                _blurRegions.clear()
                _blurRegions.addAll(
                    _detectedPlates.map { plate ->
                        BlurRegion(
                            id = plate.id,
                            boundingBox = plate.boundingBox,
                            type = BlurType.PIXELATION, // Default to pixelation for plates
                            source = DetectionSource.AUTO_LICENSE_PLATE
                        )
                    }
                )

                _uiState.value = LicensePlateBlurUiState.PlatesDetected(
                    bitmap = bitmap,
                    detectedPlates = _detectedPlates,
                    blurRegions = _blurRegions.toList()
                )
            } catch (e: Exception) {
                _uiState.value = LicensePlateBlurUiState.Error(e.message ?: "License plate detection failed")
            }
        }
    }

    /**
     * Change blur type for a specific region
     */
    fun changeBlurType(regionId: String, blurType: BlurType) {
        val index = _blurRegions.indexOfFirst { it.id == regionId }
        if (index >= 0) {
            _blurRegions[index] = _blurRegions[index].copy(type = blurType)
            updatePreview()
        }
    }

    /**
     * Change blur type for all regions at once
     */
    fun changeAllBlurTypes(blurType: BlurType) {
        _blurRegions.forEachIndexed { index, region ->
            _blurRegions[index] = region.copy(type = blurType)
        }
        updatePreview()
    }

    /**
     * Remove a specific blur region
     */
    fun removeBlurRegion(regionId: String) {
        _blurRegions.removeIf { it.id == regionId }
        _detectedPlates.removeIf { it.id == regionId }
        updatePreview()
    }

    /**
     * Apply all blur effects to the image
     */
    fun applyBlurs() {
        viewModelScope.launch {
            try {
                val bitmap = originalBitmap ?: return@launch
                _uiState.value = LicensePlateBlurUiState.Processing

                val blurred = blurProcessor.applyAllBlurs(bitmap, _blurRegions)

                _uiState.value = LicensePlateBlurUiState.BlurApplied(blurred)
            } catch (e: Exception) {
                _uiState.value = LicensePlateBlurUiState.Error(e.message ?: "Blur failed")
            }
        }
    }

    /**
     * Update preview with current blur regions
     */
    private fun updatePreview() {
        val bitmap = originalBitmap ?: return
        _uiState.value = LicensePlateBlurUiState.PlatesDetected(
            bitmap = bitmap,
            detectedPlates = _detectedPlates,
            blurRegions = _blurRegions.toList()
        )
    }

    /**
     * Reset to image loaded state
     */
    fun reset() {
        _detectedPlates.clear()
        _blurRegions.clear()
        originalBitmap?.let {
            _uiState.value = LicensePlateBlurUiState.ImageLoaded(it)
        }
    }

    /**
     * Get country hint for a detected plate
     */
    fun getCountryHint(plateId: String): String? {
        val plate = _detectedPlates.find { it.id == plateId }
        return plate?.let { licensePlateDetector.guessCountryFromPlate(it.text) }
    }

    override fun onCleared() {
        super.onCleared()
        textRecognitionManager.release()
        originalBitmap?.recycle()
    }
}

/**
 * UI State for license plate blur editing
 */
sealed class LicensePlateBlurUiState {
    data object Idle : LicensePlateBlurUiState()
    data object Loading : LicensePlateBlurUiState()
    data class ImageLoaded(val bitmap: Bitmap) : LicensePlateBlurUiState()
    data object Detecting : LicensePlateBlurUiState()
    data class PlatesDetected(
        val bitmap: Bitmap,
        val detectedPlates: List<DetectedText>,
        val blurRegions: List<BlurRegion>
    ) : LicensePlateBlurUiState()
    data object Processing : LicensePlateBlurUiState()
    data class BlurApplied(val bitmap: Bitmap) : LicensePlateBlurUiState()
    data class Error(val message: String) : LicensePlateBlurUiState()
}
