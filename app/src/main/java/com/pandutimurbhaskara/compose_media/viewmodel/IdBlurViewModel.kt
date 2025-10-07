package com.pandutimurbhaskara.compose_media.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandutimurbhaskara.compose_media.ml.IdCardDetector
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
 * ViewModel for managing ID card blur editing state
 */
class IdBlurViewModel(
    private val textRecognitionManager: TextRecognitionManager = TextRecognitionManager(),
    private val idCardDetector: IdCardDetector = IdCardDetector(textRecognitionManager),
    private val blurProcessor: BlurProcessor = BlurProcessor(),
    private val imageProcessor: ImageProcessor? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<IdBlurUiState>(IdBlurUiState.Idle)
    val uiState: StateFlow<IdBlurUiState> = _uiState.asStateFlow()

    private var originalBitmap: Bitmap? = null
    private val _detectedTexts = mutableListOf<DetectedText>()
    private val _blurRegions = mutableListOf<BlurRegion>()

    /**
     * Load image from URI and prepare for editing
     */
    fun loadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = IdBlurUiState.Loading

                val processor = imageProcessor ?: ImageProcessor(context)
                originalBitmap = processor.loadAndOptimizeImage(uri)

                _uiState.value = IdBlurUiState.ImageLoaded(originalBitmap!!)
            } catch (e: Exception) {
                _uiState.value = IdBlurUiState.Error(e.message ?: "Failed to load image")
            }
        }
    }

    /**
     * Detect ID card information in the loaded image
     */
    fun detectIdCard() {
        viewModelScope.launch {
            try {
                val bitmap = originalBitmap ?: return@launch
                _uiState.value = IdBlurUiState.Detecting

                _detectedTexts.clear()
                _detectedTexts.addAll(idCardDetector.detectIdCard(bitmap))

                // Convert detected texts to blur regions
                _blurRegions.clear()
                _blurRegions.addAll(
                    _detectedTexts.map { text ->
                        BlurRegion(
                            id = text.id,
                            boundingBox = text.boundingBox,
                            type = BlurType.BLACK_BOX, // Default to black box for IDs
                            source = DetectionSource.AUTO_ID_CARD
                        )
                    }
                )

                _uiState.value = IdBlurUiState.IdDetected(
                    bitmap = bitmap,
                    detectedTexts = _detectedTexts,
                    blurRegions = _blurRegions.toList()
                )
            } catch (e: Exception) {
                _uiState.value = IdBlurUiState.Error(e.message ?: "ID detection failed")
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
        updatePreview()
    }

    /**
     * Apply all blur effects to the image
     */
    fun applyBlurs() {
        viewModelScope.launch {
            try {
                val bitmap = originalBitmap ?: return@launch
                _uiState.value = IdBlurUiState.Processing

                val blurred = blurProcessor.applyAllBlurs(bitmap, _blurRegions)

                _uiState.value = IdBlurUiState.BlurApplied(blurred)
            } catch (e: Exception) {
                _uiState.value = IdBlurUiState.Error(e.message ?: "Blur failed")
            }
        }
    }

    /**
     * Update preview with current blur regions
     */
    private fun updatePreview() {
        val bitmap = originalBitmap ?: return
        _uiState.value = IdBlurUiState.IdDetected(
            bitmap = bitmap,
            detectedTexts = _detectedTexts,
            blurRegions = _blurRegions.toList()
        )
    }

    /**
     * Reset to image loaded state
     */
    fun reset() {
        _detectedTexts.clear()
        _blurRegions.clear()
        originalBitmap?.let {
            _uiState.value = IdBlurUiState.ImageLoaded(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        textRecognitionManager.release()
        originalBitmap?.recycle()
    }
}

/**
 * UI State for ID card blur editing
 */
sealed class IdBlurUiState {
    data object Idle : IdBlurUiState()
    data object Loading : IdBlurUiState()
    data class ImageLoaded(val bitmap: Bitmap) : IdBlurUiState()
    data object Detecting : IdBlurUiState()
    data class IdDetected(
        val bitmap: Bitmap,
        val detectedTexts: List<DetectedText>,
        val blurRegions: List<BlurRegion>
    ) : IdBlurUiState()
    data object Processing : IdBlurUiState()
    data class BlurApplied(val bitmap: Bitmap) : IdBlurUiState()
    data class Error(val message: String) : IdBlurUiState()
}
