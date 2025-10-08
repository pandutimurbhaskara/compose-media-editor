package com.pandutimurbhaskara.compose_media.ui.components

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.pandutimurbhaskara.compose_media.model.BlurRegion
import com.pandutimurbhaskara.compose_media.model.BlurType
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Interactive bounding box editor with drag, resize, and add capabilities
 */
@Composable
fun BoundingBoxEditor(
    bitmap: Bitmap,
    blurRegions: List<BlurRegion>,
    onRegionUpdate: (BlurRegion) -> Unit,
    onRegionRemove: (String) -> Unit,
    onAddRegion: (Rect, BlurType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRegionId by remember { mutableStateOf<String?>(null) }
    var isDrawingNew by remember { mutableStateOf(false) }
    var newBoxStart by remember { mutableStateOf<Offset?>(null) }
    var newBoxEnd by remember { mutableStateOf<Offset?>(null) }
    var draggedRegion by remember { mutableStateOf<BlurRegion?>(null) }
    var resizeHandle by remember { mutableStateOf<ResizeHandle?>(null) }

    // Calculate scale and offset for the bitmap
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            // Check if tapped on existing box
                            val tapped = blurRegions.find { region ->
                                isPointInScaledRect(
                                    tapOffset,
                                    region.boundingBox,
                                    scale,
                                    offsetX,
                                    offsetY
                                )
                            }

                            selectedRegionId = tapped?.id
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Check if touching a resize handle
                            selectedRegionId?.let { id ->
                                val region = blurRegions.find { it.id == id }
                                region?.let {
                                    val handle = getResizeHandleAt(
                                        offset,
                                        it.boundingBox,
                                        scale,
                                        offsetX,
                                        offsetY
                                    )
                                    if (handle != null) {
                                        resizeHandle = handle
                                        draggedRegion = it
                                        return@detectDragGestures
                                    }
                                }
                            }

                            // Check if touching existing box for moving
                            val touched = blurRegions.find { region ->
                                isPointInScaledRect(
                                    offset,
                                    region.boundingBox,
                                    scale,
                                    offsetX,
                                    offsetY
                                )
                            }

                            if (touched != null) {
                                selectedRegionId = touched.id
                                draggedRegion = touched
                            } else {
                                // Start drawing new box
                                isDrawingNew = true
                                newBoxStart = offset
                                newBoxEnd = offset
                                selectedRegionId = null
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (isDrawingNew) {
                                // Update new box
                                newBoxEnd = change.position
                            } else if (resizeHandle != null && draggedRegion != null) {
                                // Resize box
                                val newRect = resizeRect(
                                    draggedRegion!!.boundingBox,
                                    resizeHandle!!,
                                    dragAmount,
                                    scale
                                )
                                onRegionUpdate(draggedRegion!!.copy(boundingBox = newRect))
                            } else if (draggedRegion != null) {
                                // Move box
                                val scaledDragAmount = dragAmount / scale
                                val newRect = Rect(
                                    (draggedRegion!!.boundingBox.left + scaledDragAmount.x).toInt()
                                        .coerceIn(0, bitmap.width),
                                    (draggedRegion!!.boundingBox.top + scaledDragAmount.y).toInt()
                                        .coerceIn(0, bitmap.height),
                                    (draggedRegion!!.boundingBox.right + scaledDragAmount.x).toInt()
                                        .coerceIn(0, bitmap.width),
                                    (draggedRegion!!.boundingBox.bottom + scaledDragAmount.y).toInt()
                                        .coerceIn(0, bitmap.height)
                                )
                                onRegionUpdate(draggedRegion!!.copy(boundingBox = newRect))
                            }
                        },
                        onDragEnd = {
                            if (isDrawingNew) {
                                // Create new blur region
                                val start = newBoxStart
                                val end = newBoxEnd
                                if (start != null && end != null) {
                                    val rect = createRectFromPoints(
                                        start,
                                        end,
                                        scale,
                                        offsetX,
                                        offsetY,
                                        bitmap.width,
                                        bitmap.height
                                    )
                                    // Only create if box is large enough (> 20px)
                                    if (rect.width() > 20 && rect.height() > 20) {
                                        onAddRegion(rect, BlurType.GAUSSIAN)
                                    }
                                }
                                isDrawingNew = false
                                newBoxStart = null
                                newBoxEnd = null
                            }
                            draggedRegion = null
                            resizeHandle = null
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bitmapWidth = bitmap.width
            val bitmapHeight = bitmap.height

            // Calculate scaling to fit image in canvas
            scale = minOf(
                canvasWidth / bitmapWidth,
                canvasHeight / bitmapHeight
            )

            val scaledWidth = bitmapWidth * scale
            val scaledHeight = bitmapHeight * scale

            // Center the image
            offsetX = (canvasWidth - scaledWidth) / 2
            offsetY = (canvasHeight - scaledHeight) / 2

            // Draw image
            drawImage(
                image = bitmap.asImageBitmap(),
                dstOffset = androidx.compose.ui.unit.IntOffset(
                    offsetX.toInt(),
                    offsetY.toInt()
                ),
                dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
            )

            // Draw existing blur regions
            blurRegions.forEach { region ->
                drawBlurRegion(
                    region = region,
                    isSelected = region.id == selectedRegionId,
                    scale = scale,
                    offsetX = offsetX,
                    offsetY = offsetY
                )
            }

            // Draw new box being created
            if (isDrawingNew && newBoxStart != null && newBoxEnd != null) {
                drawNewBox(newBoxStart!!, newBoxEnd!!)
            }
        }

        // Region controls (show when a region is selected)
        selectedRegionId?.let { id ->
            val region = blurRegions.find { it.id == id }
            region?.let {
                RegionControls(
                    regionId = id,
                    currentType = it.type,
                    onTypeChange = { type ->
                        onRegionUpdate(it.copy(type = type))
                    },
                    onDelete = { onRegionRemove(id) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

/**
 * Draw a blur region with bounding box and resize handles
 */
private fun DrawScope.drawBlurRegion(
    region: BlurRegion,
    isSelected: Boolean,
    scale: Float,
    offsetX: Float,
    offsetY: Float
) {
    val color = when (region.type) {
        BlurType.GAUSSIAN -> Color(0xFF2196F3) // Blue
        BlurType.PIXELATION -> Color(0xFF4CAF50) // Green
        BlurType.BLACK_BOX -> Color(0xFFF44336) // Red
    }

    val left = region.boundingBox.left * scale + offsetX
    val top = region.boundingBox.top * scale + offsetY
    val width = region.boundingBox.width() * scale
    val height = region.boundingBox.height() * scale

    // Draw bounding box
    drawRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(width = if (isSelected) 6f else 4f)
    )

    // Draw resize handles for selected box
    if (isSelected) {
        val handleSize = 24f
        val handles = listOf(
            Offset(left - handleSize / 2, top - handleSize / 2), // Top-left
            Offset(left + width - handleSize / 2, top - handleSize / 2), // Top-right
            Offset(left - handleSize / 2, top + height - handleSize / 2), // Bottom-left
            Offset(left + width - handleSize / 2, top + height - handleSize / 2), // Bottom-right
        )

        handles.forEach { handlePos ->
            drawCircle(
                color = Color.White,
                radius = handleSize / 2,
                center = Offset(handlePos.x + handleSize / 2, handlePos.y + handleSize / 2)
            )
            drawCircle(
                color = color,
                radius = handleSize / 2,
                center = Offset(handlePos.x + handleSize / 2, handlePos.y + handleSize / 2),
                style = Stroke(width = 3f)
            )
        }
    }
}

/**
 * Draw new box being created
 */
private fun DrawScope.drawNewBox(start: Offset, end: Offset) {
    val left = min(start.x, end.x)
    val top = min(start.y, end.y)
    val width = abs(end.x - start.x)
    val height = abs(end.y - start.y)

    drawRect(
        color = Color.Cyan,
        topLeft = Offset(left, top),
        size = Size(width, height),
        style = Stroke(width = 4f)
    )
}

/**
 * Check if a point is inside a scaled rectangle
 */
private fun isPointInScaledRect(
    point: Offset,
    rect: Rect,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): Boolean {
    val left = rect.left * scale + offsetX
    val top = rect.top * scale + offsetY
    val right = rect.right * scale + offsetX
    val bottom = rect.bottom * scale + offsetY

    return point.x >= left && point.x <= right && point.y >= top && point.y <= bottom
}

/**
 * Get resize handle at a position, if any
 */
private fun getResizeHandleAt(
    point: Offset,
    rect: Rect,
    scale: Float,
    offsetX: Float,
    offsetY: Float
): ResizeHandle? {
    val handleSize = 24f
    val left = rect.left * scale + offsetX
    val top = rect.top * scale + offsetY
    val right = rect.right * scale + offsetX
    val bottom = rect.bottom * scale + offsetY

    // Check each corner
    if (isPointNear(point, Offset(left, top), handleSize)) {
        return ResizeHandle.TOP_LEFT
    }
    if (isPointNear(point, Offset(right, top), handleSize)) {
        return ResizeHandle.TOP_RIGHT
    }
    if (isPointNear(point, Offset(left, bottom), handleSize)) {
        return ResizeHandle.BOTTOM_LEFT
    }
    if (isPointNear(point, Offset(right, bottom), handleSize)) {
        return ResizeHandle.BOTTOM_RIGHT
    }

    return null
}

/**
 * Check if a point is near a target position
 */
private fun isPointNear(point: Offset, target: Offset, threshold: Float): Boolean {
    val dx = point.x - target.x
    val dy = point.y - target.y
    return (dx * dx + dy * dy) <= (threshold * threshold)
}

/**
 * Resize a rectangle based on which handle is being dragged
 */
private fun resizeRect(
    rect: Rect,
    handle: ResizeHandle,
    dragAmount: Offset,
    scale: Float
): Rect {
    val scaledDrag = dragAmount / scale

    return when (handle) {
        ResizeHandle.TOP_LEFT -> Rect(
            (rect.left + scaledDrag.x).toInt(),
            (rect.top + scaledDrag.y).toInt(),
            rect.right,
            rect.bottom
        )
        ResizeHandle.TOP_RIGHT -> Rect(
            rect.left,
            (rect.top + scaledDrag.y).toInt(),
            (rect.right + scaledDrag.x).toInt(),
            rect.bottom
        )
        ResizeHandle.BOTTOM_LEFT -> Rect(
            (rect.left + scaledDrag.x).toInt(),
            rect.top,
            rect.right,
            (rect.bottom + scaledDrag.y).toInt()
        )
        ResizeHandle.BOTTOM_RIGHT -> Rect(
            rect.left,
            rect.top,
            (rect.right + scaledDrag.x).toInt(),
            (rect.bottom + scaledDrag.y).toInt()
        )
    }
}

/**
 * Create a Rect from two points, converting from screen to bitmap coordinates
 */
private fun createRectFromPoints(
    start: Offset,
    end: Offset,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    bitmapWidth: Int,
    bitmapHeight: Int
): Rect {
    // Convert screen coordinates to bitmap coordinates
    val x1 = ((start.x - offsetX) / scale).toInt().coerceIn(0, bitmapWidth)
    val y1 = ((start.y - offsetY) / scale).toInt().coerceIn(0, bitmapHeight)
    val x2 = ((end.x - offsetX) / scale).toInt().coerceIn(0, bitmapWidth)
    val y2 = ((end.y - offsetY) / scale).toInt().coerceIn(0, bitmapHeight)

    return Rect(
        min(x1, x2),
        min(y1, y2),
        max(x1, x2),
        max(y1, y2)
    )
}

/**
 * Resize handle positions
 */
private enum class ResizeHandle {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

/**
 * Controls for selected region
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionControls(
    regionId: String,
    currentType: BlurType,
    onTypeChange: (BlurType) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(Spacing.medium),
        shape = RoundedCornerShape(Spacing.medium),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BlurType.values().forEach { type ->
                FilterChip(
                    selected = currentType == type,
                    onClick = { onTypeChange(type) },
                    label = {
                        Text(
                            when (type) {
                                BlurType.GAUSSIAN -> "Gaussian"
                                BlurType.PIXELATION -> "Pixel"
                                BlurType.BLACK_BOX -> "Black"
                            }
                        )
                    }
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
