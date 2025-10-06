package com.pandutimurbhaskara.compose_media.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Utility class for image loading, optimization, and preprocessing
 */
class ImageProcessor(private val context: Context) {

    /**
     * Load and optimize image from URI
     * - Handles image rotation based on EXIF data
     * - Optimizes image size for ML processing
     * - Maximum size: 2048px on longest edge
     *
     * @param uri URI of the image to load
     * @return Optimized and correctly oriented Bitmap
     */
    suspend fun loadAndOptimizeImage(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply {
            // First decode to get dimensions only
            inJustDecodeBounds = true
        }

        // Get image dimensions
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // Calculate appropriate sample size (max 2048px on longest edge)
        val maxSize = 2048
        options.inSampleSize = calculateInSampleSize(options, maxSize, maxSize)
        options.inJustDecodeBounds = false

        // Decode actual bitmap with sample size
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: throw IOException("Failed to load image from URI: $uri")

        // Fix orientation based on EXIF data
        fixImageOrientation(bitmap, uri)
    }

    /**
     * Calculate sample size for image decoding
     * Reduces memory usage while maintaining acceptable quality
     *
     * @param options BitmapFactory options with image dimensions
     * @param reqWidth Required width
     * @param reqHeight Required height
     * @return Sample size (power of 2)
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2
            // and keeps both height and width larger than requested
            while (halfHeight / inSampleSize >= reqHeight &&
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Fix image orientation based on EXIF metadata
     * Many cameras save images in landscape but mark them as rotated
     *
     * @param bitmap Original bitmap
     * @param uri URI to read EXIF data from
     * @return Bitmap with correct orientation
     */
    private suspend fun fixImageOrientation(bitmap: Bitmap, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        val exif = try {
            context.contentResolver.openInputStream(uri)?.use {
                ExifInterface(it)
            }
        } catch (e: Exception) {
            null
        }

        val orientation = exif?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        ) ?: ExifInterface.ORIENTATION_NORMAL

        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(bitmap, horizontal = true)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(bitmap, horizontal = false)
            else -> bitmap
        }
    }

    /**
     * Rotate bitmap by specified degrees
     *
     * @param bitmap Original bitmap
     * @param degrees Rotation angle (90, 180, 270)
     * @return Rotated bitmap
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    /**
     * Flip bitmap horizontally or vertically
     *
     * @param bitmap Original bitmap
     * @param horizontal True for horizontal flip, false for vertical
     * @return Flipped bitmap
     */
    private fun flipBitmap(bitmap: Bitmap, horizontal: Boolean): Bitmap {
        val matrix = Matrix().apply {
            if (horizontal) {
                postScale(-1f, 1f)
            } else {
                postScale(1f, -1f)
            }
        }
        return Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }

    /**
     * Get image dimensions without loading full bitmap
     *
     * @param uri URI of the image
     * @return Pair of (width, height)
     */
    suspend fun getImageDimensions(uri: Uri): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        options.outWidth to options.outHeight
    }
}
