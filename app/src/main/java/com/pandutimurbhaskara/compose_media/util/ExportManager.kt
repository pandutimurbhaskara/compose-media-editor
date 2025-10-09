package com.pandutimurbhaskara.compose_media.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Manager for exporting images and managing drafts
 */
class ExportManager(private val context: Context) {

    /**
     * Save bitmap to device gallery
     * @param bitmap The image to save
     * @param fileName Optional custom filename
     * @return Uri of saved image
     */
    suspend fun saveToGallery(
        bitmap: Bitmap,
        fileName: String = generateFileName()
    ): Uri = withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MediaEditor")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw IOException("Failed to create MediaStore entry")

        resolver.openOutputStream(imageUri)?.use { outputStream ->
            // Save with 95% quality for minimal quality loss
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
        } ?: throw IOException("Failed to open output stream")

        imageUri
    }

    /**
     * Save bitmap to app-specific storage (for drafts)
     * @param bitmap The image to save
     * @return File path of saved draft
     */
    suspend fun saveDraft(
        bitmap: Bitmap,
        originalUri: Uri? = null
    ): DraftFile = withContext(Dispatchers.IO) {
        val draftId = UUID.randomUUID().toString()
        val draftDir = File(context.filesDir, "drafts")
        draftDir.mkdirs()

        val draftFile = File(draftDir, "$draftId.jpg")

        FileOutputStream(draftFile).use { outputStream ->
            // Save with 90% quality for drafts (smaller file size)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }

        DraftFile(
            id = draftId,
            filePath = draftFile.absolutePath,
            fileName = draftFile.name,
            timestamp = System.currentTimeMillis(),
            originalUri = originalUri?.toString()
        )
    }

    /**
     * Load draft bitmap from file
     * @param draftFile The draft file info
     * @return Bitmap or null if failed
     */
    suspend fun loadDraft(draftFile: DraftFile): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(draftFile.filePath)
            if (file.exists()) {
                android.graphics.BitmapFactory.decodeFile(file.absolutePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Delete a draft file
     * @param draftId The draft ID to delete
     */
    suspend fun deleteDraft(draftId: String): Boolean = withContext(Dispatchers.IO) {
        val draftDir = File(context.filesDir, "drafts")
        val draftFile = File(draftDir, "$draftId.jpg")
        draftFile.delete()
    }

    /**
     * List all draft files
     * @return List of draft files
     */
    suspend fun listDrafts(): List<DraftFile> = withContext(Dispatchers.IO) {
        val draftDir = File(context.filesDir, "drafts")
        if (!draftDir.exists()) {
            return@withContext emptyList()
        }

        draftDir.listFiles()?.mapNotNull { file ->
            if (file.extension == "jpg") {
                DraftFile(
                    id = file.nameWithoutExtension,
                    filePath = file.absolutePath,
                    fileName = file.name,
                    timestamp = file.lastModified(),
                    originalUri = null
                )
            } else {
                null
            }
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    /**
     * Get total size of all drafts
     * @return Size in bytes
     */
    suspend fun getDraftsSize(): Long = withContext(Dispatchers.IO) {
        val draftDir = File(context.filesDir, "drafts")
        if (!draftDir.exists()) return@withContext 0L

        draftDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Clear all drafts
     * @return Number of drafts deleted
     */
    suspend fun clearAllDrafts(): Int = withContext(Dispatchers.IO) {
        val draftDir = File(context.filesDir, "drafts")
        if (!draftDir.exists()) return@withContext 0

        val files = draftDir.listFiles() ?: return@withContext 0
        var count = 0
        files.forEach { file ->
            if (file.delete()) count++
        }
        count
    }

    /**
     * Generate a unique filename with timestamp
     */
    private fun generateFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "MediaEditor_$timestamp.jpg"
    }

    /**
     * Format timestamp to human-readable string
     */
    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000} minutes ago"
            diff < 86400_000 -> "${diff / 3600_000} hours ago"
            diff < 604800_000 -> "${diff / 86400_000} days ago"
            else -> SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(timestamp))
        }
    }

    /**
     * Format file size to human-readable string
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}

/**
 * Data class for draft file information
 */
data class DraftFile(
    val id: String,
    val filePath: String,
    val fileName: String,
    val timestamp: Long,
    val originalUri: String?
)
