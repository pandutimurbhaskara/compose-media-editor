package com.pandutimurbhaskara.compose_media.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.pandutimurbhaskara.compose_media.model.BlurRegion
import com.pandutimurbhaskara.compose_media.model.BlurType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Processor for applying various blur effects to images
 * Supports: Gaussian Blur, Pixelation, and Black Box
 */
class BlurProcessor {

    /**
     * Apply Gaussian blur to a specific region
     * Uses a fast blur algorithm for performance
     */
    suspend fun applyGaussianBlur(
        bitmap: Bitmap,
        region: Rect,
        blurRadius: Float = 25f
    ): Bitmap = withContext(Dispatchers.Default) {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // Extract the region to blur
        val regionBitmap = Bitmap.createBitmap(
            mutableBitmap,
            region.left.coerceAtLeast(0),
            region.top.coerceAtLeast(0),
            region.width().coerceAtMost(bitmap.width - region.left),
            region.height().coerceAtMost(bitmap.height - region.top)
        )

        // Apply fast blur algorithm
        val blurred = fastBlur(regionBitmap, blurRadius.toInt())

        // Draw blurred region back to original image
        val canvas = Canvas(mutableBitmap)
        canvas.drawBitmap(blurred, region.left.toFloat(), region.top.toFloat(), null)

        regionBitmap.recycle()
        blurred.recycle()

        mutableBitmap
    }

    /**
     * Apply pixelation effect to a specific region
     * Creates a mosaic/pixelated appearance
     */
    suspend fun applyPixelation(
        bitmap: Bitmap,
        region: Rect,
        pixelSize: Int = 20
    ): Bitmap = withContext(Dispatchers.Default) {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint()

        // Iterate through region in pixel blocks
        var y = region.top
        while (y < region.bottom) {
            var x = region.left
            while (x < region.right) {
                // Get color of pixel at this position
                val pixelX = x.coerceIn(0, bitmap.width - 1)
                val pixelY = y.coerceIn(0, bitmap.height - 1)
                val pixelColor = bitmap.getPixel(pixelX, pixelY)

                paint.color = pixelColor

                // Draw filled rectangle for this pixel block
                canvas.drawRect(
                    x.toFloat(),
                    y.toFloat(),
                    (x + pixelSize).toFloat().coerceAtMost(region.right.toFloat()),
                    (y + pixelSize).toFloat().coerceAtMost(region.bottom.toFloat()),
                    paint
                )

                x += pixelSize
            }
            y += pixelSize
        }

        mutableBitmap
    }

    /**
     * Apply solid black box to a specific region
     * Completely obscures the region
     */
    suspend fun applyBlackBox(
        bitmap: Bitmap,
        region: Rect
    ): Bitmap = withContext(Dispatchers.Default) {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        canvas.drawRect(region, paint)

        mutableBitmap
    }

    /**
     * Apply all blur regions to an image
     * Processes each region based on its blur type
     */
    suspend fun applyAllBlurs(
        originalBitmap: Bitmap,
        blurRegions: List<BlurRegion>
    ): Bitmap = withContext(Dispatchers.Default) {
        var result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)

        blurRegions.forEach { region ->
            val temp = when (region.type) {
                BlurType.GAUSSIAN -> applyGaussianBlur(result, region.boundingBox)
                BlurType.PIXELATION -> applyPixelation(result, region.boundingBox)
                BlurType.BLACK_BOX -> applyBlackBox(result, region.boundingBox)
            }

            // Recycle previous bitmap if it's not the original
            if (result != originalBitmap) {
                result.recycle()
            }
            result = temp
        }

        result
    }

    /**
     * Fast blur algorithm implementation
     * Stack blur algorithm - efficient approximation of Gaussian blur
     */
    private fun fastBlur(bitmap: Bitmap, radius: Int): Bitmap {
        if (radius < 1) return bitmap

        val w = bitmap.width
        val h = bitmap.height

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int
        var gsum: Int
        var bsum: Int
        var x: Int
        var y: Int
        var i: Int
        var p: Int
        var yp: Int
        var yi: Int
        var yw: Int

        val vmin = IntArray(w.coerceAtLeast(h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        i = 0
        while (i < 256 * divsum) {
            dv[i] = i / divsum
            i++
        }

        yw = 0
        yi = 0

        val stack = Array(div) { IntArray(3) }
        var stackpointer: Int
        var stackstart: Int
        var sir: IntArray
        var rbs: Int
        val r1 = radius + 1
        var routsum: Int
        var goutsum: Int
        var boutsum: Int
        var rinsum: Int
        var ginsum: Int
        var binsum: Int

        y = 0
        while (y < h) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum

            i = -radius
            while (i <= radius) {
                p = pixels[yi + (wm.coerceAtMost(i.coerceAtLeast(0)))]
                sir = stack[i + radius]
                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff
                rbs = r1 - kotlin.math.abs(i)
                rsum += sir[0] * rbs
                gsum += sir[1] * rbs
                bsum += sir[2] * rbs
                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }
                i++
            }
            stackpointer = radius

            x = 0
            while (x < w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (y == 0) {
                    vmin[x] = (x + radius + 1).coerceAtMost(wm)
                }
                p = pixels[yw + vmin[x]]

                sir[0] = (p and 0xff0000) shr 16
                sir[1] = (p and 0x00ff00) shr 8
                sir[2] = p and 0x0000ff

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer % div]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi++
                x++
            }
            yw += w
            y++
        }

        x = 0
        while (x < w) {
            bsum = 0
            gsum = bsum
            rsum = gsum
            boutsum = rsum
            goutsum = boutsum
            routsum = goutsum
            binsum = routsum
            ginsum = binsum
            rinsum = ginsum
            yp = -radius * w
            i = -radius
            while (i <= radius) {
                yi = (yp + x).coerceAtLeast(0)

                sir = stack[i + radius]

                sir[0] = r[yi]
                sir[1] = g[yi]
                sir[2] = b[yi]

                rbs = r1 - kotlin.math.abs(i)

                rsum += r[yi] * rbs
                gsum += g[yi] * rbs
                bsum += b[yi] * rbs

                if (i > 0) {
                    rinsum += sir[0]
                    ginsum += sir[1]
                    binsum += sir[2]
                } else {
                    routsum += sir[0]
                    goutsum += sir[1]
                    boutsum += sir[2]
                }

                if (i < hm) {
                    yp += w
                }
                i++
            }
            yi = x
            stackpointer = radius
            y = 0
            while (y < h) {
                pixels[yi] = -0x1000000 and pixels[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]

                rsum -= routsum
                gsum -= goutsum
                bsum -= boutsum

                stackstart = stackpointer - radius + div
                sir = stack[stackstart % div]

                routsum -= sir[0]
                goutsum -= sir[1]
                boutsum -= sir[2]

                if (x == 0) {
                    vmin[y] = (y + r1).coerceAtMost(hm) * w
                }
                p = x + vmin[y]

                sir[0] = r[p]
                sir[1] = g[p]
                sir[2] = b[p]

                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]

                rsum += rinsum
                gsum += ginsum
                bsum += binsum

                stackpointer = (stackpointer + 1) % div
                sir = stack[stackpointer]

                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]

                rinsum -= sir[0]
                ginsum -= sir[1]
                binsum -= sir[2]

                yi += w
                y++
            }
            x++
        }

        val blurred = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        blurred.setPixels(pixels, 0, w, 0, 0, w, h)

        return blurred
    }
}
