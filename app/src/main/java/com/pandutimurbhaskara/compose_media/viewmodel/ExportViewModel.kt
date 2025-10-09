package com.pandutimurbhaskara.compose_media.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandutimurbhaskara.compose_media.util.DraftFile
import com.pandutimurbhaskara.compose_media.util.ExportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing export and save operations
 */
class ExportViewModel(
    private val exportManager: ExportManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    /**
     * Save bitmap to gallery
     */
    fun saveToGallery(bitmap: Bitmap, context: Context, fileName: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = ExportUiState.Saving

                val manager = exportManager ?: ExportManager(context)
                val uri = if (fileName != null) {
                    manager.saveToGallery(bitmap, fileName)
                } else {
                    manager.saveToGallery(bitmap)
                }

                _uiState.value = ExportUiState.SavedToGallery(uri)
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "Failed to save image")
            }
        }
    }

    /**
     * Save as draft
     */
    fun saveDraft(bitmap: Bitmap, context: Context, originalUri: Uri? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = ExportUiState.Saving

                val manager = exportManager ?: ExportManager(context)
                val draftFile = manager.saveDraft(bitmap, originalUri)

                _uiState.value = ExportUiState.SavedAsDraft(draftFile)
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "Failed to save draft")
            }
        }
    }

    /**
     * Load all drafts
     */
    fun loadDrafts(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = ExportUiState.Loading

                val manager = exportManager ?: ExportManager(context)
                val drafts = manager.listDrafts()

                _uiState.value = ExportUiState.DraftsLoaded(drafts)
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "Failed to load drafts")
            }
        }
    }

    /**
     * Load a specific draft
     */
    fun loadDraft(draftFile: DraftFile, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = ExportUiState.Loading

                val manager = exportManager ?: ExportManager(context)
                val bitmap = manager.loadDraft(draftFile)

                if (bitmap != null) {
                    _uiState.value = ExportUiState.DraftLoaded(bitmap, draftFile)
                } else {
                    _uiState.value = ExportUiState.Error("Failed to load draft")
                }
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "Failed to load draft")
            }
        }
    }

    /**
     * Delete a draft
     */
    fun deleteDraft(draftId: String, context: Context) {
        viewModelScope.launch {
            try {
                val manager = exportManager ?: ExportManager(context)
                val success = manager.deleteDraft(draftId)

                if (success) {
                    // Reload drafts
                    loadDrafts(context)
                } else {
                    _uiState.value = ExportUiState.Error("Failed to delete draft")
                }
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "Failed to delete draft")
            }
        }
    }

    /**
     * Get drafts storage info
     */
    fun getDraftsInfo(context: Context) {
        viewModelScope.launch {
            try {
                val manager = exportManager ?: ExportManager(context)
                val size = manager.getDraftsSize()
                val drafts = manager.listDrafts()

                _uiState.value = ExportUiState.DraftsInfo(
                    totalSize = size,
                    draftCount = drafts.size,
                    formattedSize = manager.formatFileSize(size)
                )
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "Failed to get drafts info")
            }
        }
    }

    /**
     * Clear all drafts
     */
    fun clearAllDrafts(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = ExportUiState.Saving

                val manager = exportManager ?: ExportManager(context)
                val deletedCount = manager.clearAllDrafts()

                _uiState.value = ExportUiState.DraftsCleared(deletedCount)
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error(e.message ?: "Failed to clear drafts")
            }
        }
    }

    /**
     * Reset state to idle
     */
    fun resetState() {
        _uiState.value = ExportUiState.Idle
    }
}

/**
 * UI State for export operations
 */
sealed class ExportUiState {
    data object Idle : ExportUiState()
    data object Loading : ExportUiState()
    data object Saving : ExportUiState()
    data class SavedToGallery(val uri: Uri) : ExportUiState()
    data class SavedAsDraft(val draftFile: DraftFile) : ExportUiState()
    data class DraftsLoaded(val drafts: List<DraftFile>) : ExportUiState()
    data class DraftLoaded(val bitmap: Bitmap, val draftFile: DraftFile) : ExportUiState()
    data class DraftsInfo(
        val totalSize: Long,
        val draftCount: Int,
        val formattedSize: String
    ) : ExportUiState()
    data class DraftsCleared(val deletedCount: Int) : ExportUiState()
    data class Error(val message: String) : ExportUiState()
}
