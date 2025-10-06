package com.pandutimurbhaskara.compose_media.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper class for image picking from gallery and camera
 */
class ImagePickerHelper(private val context: Context) {

    /**
     * Create intent to pick image from gallery
     */
    fun createGalleryIntent(): Intent {
        return Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
    }

    /**
     * Create intent to capture image from camera
     * @param photoUri URI where the captured photo will be saved
     */
    fun createCameraIntent(photoUri: Uri): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
    }

    /**
     * Create a temporary file for camera capture
     * @return File object for the image
     */
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Ensure directory exists
        storageDir?.mkdirs()

        return File.createTempFile(
            "JPEG_${timeStamp}_",  // prefix
            ".jpg",                 // suffix
            storageDir              // directory
        )
    }

    /**
     * Get content URI for a file using FileProvider
     * @param file The file to get URI for
     * @return Content URI that can be shared with camera app
     */
    fun getUriForFile(file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
