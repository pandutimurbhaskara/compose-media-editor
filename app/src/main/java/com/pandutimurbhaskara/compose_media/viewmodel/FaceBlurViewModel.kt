package com.pandutimurbhaskara.compose_media.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandutimurbhaskara.compose_media.ml.FaceDetectorManager
import com.pandutimurbhaskara.compose_media.model.BlurRegion
import com.pandutimurbhaskara.compose_media.model.BlurType
import com.pandutimurbhaskara.compose_media.model.DetectedFace
import com.pandutimurbhaskara.compose_media.model.DetectionSource
import com.pandutimurbhaskara.compose_media.util.BlurProcessor
import com.pandutimurbhaskara.compose_media.util.ImageProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing face blur editing state
 */
class FaceBlurViewModel(
    private val faceDetector: FaceDetectorManager = FaceDetectorManager(),
    private val blurProcessor: BlurProcessor = BlurProcessor(),
    private val imageProcessor: ImageProcessor? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<FaceBlurUiState>(FaceBlurUiState.Idle)
    val uiState: StateFlow<FaceBlurUiState> = _uiState.asStateFlow()

    private var originalBitmap: Bitmap? = null
    private val _detectedFaces = mutableListOf<DetectedFace>()
    private val _blurRegions = mutableListOf<BlurRegion>()

    /**
     * Load image from URI and prepare for editing
     */
    fun loadImage(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = FaceBlurUiState.Loading

                val processor = imageProcessor ?: ImageProcessor(context)
                originalBitmap = processor.loadAndOptimizeImage(uri)

                _uiState.value = FaceBlurUiState.ImageLoaded(originalBitmap!!)
            } catch (e: Exception) {
                _uiState.value = FaceBlurUiState.Error(e.message ?: "Failed to load image")
            }
        }
    }

    /**
     * Detect faces in the loaded image
     */
    fun detectFaces() {
        viewModelScope.launch {
            try {
                val bitmap = originalBitmap ?: return@launch
                _uiState.value = FaceBlurUiState.Detecting

                _detectedFaces.clear()
                _detectedFaces.addAll(faceDetector.detectFaces(bitmap))

                // Convert detected faces to blur regions
                _blurRegions.clear()
                _blurRegions.addAll(
                    _detectedFaces.map { face ->
                        BlurRegion(
                            id = face.id,
                            boundingBox = face.boundingBox,
                            type = BlurType.GAUSSIAN, // Default blur type
                            source = DetectionSource.AUTO_FACE
                        )
                    }
                )

                _uiState.value = FaceBlurUiState.FacesDetected(
                    bitmap = bitmap,
                    faces = _detectedFaces,
                    blurRegions = _blurRegions.toList()
                )
            } catch (e: Exception) {
                _uiState.value = FaceBlurUiState.Error(e.message ?: "Face detection failed")
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
                _uiState.value = FaceBlurUiState.Processing

                val blurred = blurProcessor.applyAllBlurs(bitmap, _blurRegions)

                _uiState.value = FaceBlurUiState.BlurApplied(blurred)
            } catch (e: Exception) {
                _uiState.value = FaceBlurUiState.Error(e.message ?: "Blur failed")
            }
        }
    }

    /**
     * Update preview with current blur regions
     */
    private fun updatePreview() {
        val bitmap = originalBitmap ?: return
        _uiState.value = FaceBlurUiState.FacesDetected(
            bitmap = bitmap,
            faces = _detectedFaces,
            blurRegions = _blurRegions.toList()
        )
    }

    /**
     * Reset to image loaded state
     */
    fun reset() {
        _detectedFaces.clear()
        _blurRegions.clear()
        originalBitmap?.let {
            _uiState.value = FaceBlurUiState.ImageLoaded(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        faceDetector.release()
        originalBitmap?.recycle()
    }
}

/**
 * UI State for face blur editing
 */
sealed class FaceBlurUiState {
    data object Idle : FaceBlurUiState()
    data object Loading : FaceBlurUiState()
    data class ImageLoaded(val bitmap: Bitmap) : FaceBlurUiState()
    data object Detecting : FaceBlurUiState()
    data class FacesDetected(
        val bitmap: Bitmap,
        val faces: List<DetectedFace>,
        val blurRegions: List<BlurRegion>
    ) : FaceBlurUiState()
    data object Processing : FaceBlurUiState()
    data class BlurApplied(val bitmap: Bitmap) : FaceBlurUiState()
    data class Error(val message: String) : FaceBlurUiState()
}
