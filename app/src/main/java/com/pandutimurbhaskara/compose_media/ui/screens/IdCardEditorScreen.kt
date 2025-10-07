package com.pandutimurbhaskara.compose_media.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pandutimurbhaskara.compose_media.model.BlurRegion
import com.pandutimurbhaskara.compose_media.model.BlurType
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing
import com.pandutimurbhaskara.compose_media.viewmodel.IdBlurUiState
import com.pandutimurbhaskara.compose_media.viewmodel.IdBlurViewModel

/**
 * Screen for editing ID cards with blur effects
 * Detects and blurs sensitive information like ID numbers and names
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdCardEditorScreen(
    imageUri: Uri,
    viewModel: IdBlurViewModel = viewModel(),
    onSave: (Bitmap) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Load image when screen launches
    LaunchedEffect(imageUri) {
        viewModel.loadImage(imageUri, context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blur ID Card") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    // Auto detect button
                    IconButton(
                        onClick = { viewModel.detectIdCard() },
                        enabled = uiState is IdBlurUiState.ImageLoaded
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "Auto Detect")
                    }
                }
            )
        },
        bottomBar = {
            when (val state = uiState) {
                is IdBlurUiState.IdDetected -> {
                    IdBlurControlPanel(
                        blurRegions = state.blurRegions,
                        onBlurTypeChange = viewModel::changeBlurType,
                        onAllBlurTypeChange = viewModel::changeAllBlurTypes,
                        onRemoveRegion = viewModel::removeBlurRegion,
                        onApply = viewModel::applyBlurs
                    )
                }
                is IdBlurUiState.BlurApplied -> {
                    SavePanel(
                        onSave = { onSave(state.bitmap) },
                        onEdit = { viewModel.reset() },
                        onCancel = onCancel
                    )
                }
                else -> {}
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is IdBlurUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is IdBlurUiState.ImageLoaded -> {
                    ImageCanvas(bitmap = state.bitmap)
                }
                is IdBlurUiState.Detecting -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(Spacing.medium))
                        Text("Detecting ID information...")
                    }
                }
                is IdBlurUiState.IdDetected -> {
                    ImageWithBoundingBoxes(
                        bitmap = state.bitmap,
                        blurRegions = state.blurRegions
                    )
                }
                is IdBlurUiState.Processing -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(Spacing.medium))
                        Text("Applying blur...")
                    }
                }
                is IdBlurUiState.BlurApplied -> {
                    ImageCanvas(bitmap = state.bitmap)
                }
                is IdBlurUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(Spacing.medium))
                        Text(
                            text = "Tip: Ensure the ID card is clearly visible and well-lit",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

/**
 * Simple image display with Canvas
 */
@Composable
private fun ImageCanvas(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

/**
 * Display image with bounding boxes overlay for detected ID information
 */
@Composable
private fun ImageWithBoundingBoxes(
    bitmap: Bitmap,
    blurRegions: List<BlurRegion>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height

        // Calculate scaling to fit image in canvas
        val scale = minOf(
            canvasWidth / bitmapWidth,
            canvasHeight / bitmapHeight
        )

        val scaledWidth = bitmapWidth * scale
        val scaledHeight = bitmapHeight * scale

        // Center the image
        val offsetX = (canvasWidth - scaledWidth) / 2
        val offsetY = (canvasHeight - scaledHeight) / 2

        // Draw image
        drawImage(
            image = bitmap.asImageBitmap(),
            dstOffset = androidx.compose.ui.unit.IntOffset(
                offsetX.toInt(),
                offsetY.toInt()
            ),
            dstSize = IntSize(scaledWidth.toInt(), scaledHeight.toInt())
        )

        // Draw bounding boxes
        blurRegions.forEach { region ->
            val color = when (region.type) {
                BlurType.GAUSSIAN -> Color(0xFF2196F3) // Blue
                BlurType.PIXELATION -> Color(0xFF4CAF50) // Green
                BlurType.BLACK_BOX -> Color(0xFFF44336) // Red
            }

            // Scale and translate bounding box coordinates
            val left = region.boundingBox.left * scale + offsetX
            val top = region.boundingBox.top * scale + offsetY
            val width = region.boundingBox.width() * scale
            val height = region.boundingBox.height() * scale

            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = 4f)
            )
        }
    }
}

/**
 * Control panel for ID blur settings
 */
@Composable
private fun IdBlurControlPanel(
    blurRegions: List<BlurRegion>,
    onBlurTypeChange: (String, BlurType) -> Unit,
    onAllBlurTypeChange: (BlurType) -> Unit,
    onRemoveRegion: (String) -> Unit,
    onApply: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Text(
                text = "${blurRegions.size} sensitive field${if (blurRegions.size != 1) "s" else ""} detected",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(Spacing.extraSmall))

            Text(
                text = "ID numbers, names, and dates detected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.small))

            Text(
                text = "Select blur type:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.small))

            // Blur type selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                FilterChip(
                    selected = blurRegions.any { it.type == BlurType.BLACK_BOX },
                    onClick = { onAllBlurTypeChange(BlurType.BLACK_BOX) },
                    label = { Text("Black Box") }
                )
                FilterChip(
                    selected = blurRegions.any { it.type == BlurType.PIXELATION },
                    onClick = { onAllBlurTypeChange(BlurType.PIXELATION) },
                    label = { Text("Pixelation") }
                )
                FilterChip(
                    selected = blurRegions.any { it.type == BlurType.GAUSSIAN },
                    onClick = { onAllBlurTypeChange(BlurType.GAUSSIAN) },
                    label = { Text("Gaussian") }
                )
            }

            Spacer(Modifier.height(Spacing.medium))

            // Apply button
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                enabled = blurRegions.isNotEmpty()
            ) {
                Text("Apply Blur")
            }
        }
    }
}

/**
 * Panel for saving the blurred image
 */
@Composable
private fun SavePanel(
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Text(
                text = "ID information blurred successfully!",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(Spacing.extraSmall))

            Text(
                text = "Your sensitive information is now protected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(Spacing.medium))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                // Edit again button
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit Again")
                }

                // Save button
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
