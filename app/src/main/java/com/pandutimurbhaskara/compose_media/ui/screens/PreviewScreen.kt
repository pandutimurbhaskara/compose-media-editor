package com.pandutimurbhaskara.compose_media.ui.screens

import android.graphics.Bitmap
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing

/**
 * Preview screen for before/after comparison and saving
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    originalBitmap: Bitmap,
    editedBitmap: Bitmap,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean = false
) {
    var showOriginal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        bottomBar = {
            PreviewControls(
                showOriginal = showOriginal,
                onToggleView = { showOriginal = !showOriginal },
                onSave = onSave,
                isSaving = isSaving
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Display current image
            Image(
                bitmap = if (showOriginal) originalBitmap.asImageBitmap()
                        else editedBitmap.asImageBitmap(),
                contentDescription = if (showOriginal) "Original" else "Edited",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // Label showing current view
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Spacing.medium),
                shape = MaterialTheme.shapes.small,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = if (showOriginal) "Original" else "Edited",
                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.extraSmall),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            // Saving indicator
            if (isSaving) {
                Surface(
                    modifier = Modifier.align(Alignment.Center),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(Spacing.medium))
                        Text("Saving to gallery...")
                    }
                }
            }
        }
    }
}

/**
 * Bottom controls for preview
 */
@Composable
private fun PreviewControls(
    showOriginal: Boolean,
    onToggleView: () -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            // Before/After toggle
            OutlinedButton(
                onClick = onToggleView,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (showOriginal) "Show Edited" else "Show Original")
            }

            Spacer(Modifier.height(Spacing.medium))

            // Save button
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(Spacing.small))
                Text(if (isSaving) "Saving..." else "Save to Gallery")
            }
        }
    }
}

/**
 * Simple preview with just image display (for quick preview)
 */
@Composable
fun SimplePreviewScreen(
    bitmap: Bitmap,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(Spacing.medium)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Preview with comparison slider (side-by-side comparison)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonPreviewScreen(
    originalBitmap: Bitmap,
    editedBitmap: Bitmap,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Before & After") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(Spacing.small))
                    Text("Save to Gallery")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Original image
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    bitmap = originalBitmap.asImageBitmap(),
                    contentDescription = "Original",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.medium),
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = "Before",
                        modifier = Modifier.padding(
                            horizontal = Spacing.small,
                            vertical = Spacing.extraSmall
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Divider
            Spacer(
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth()
            )

            // Edited image
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Image(
                    bitmap = editedBitmap.asImageBitmap(),
                    contentDescription = "Edited",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.medium),
                    shape = MaterialTheme.shapes.small,
                    tonalElevation = 4.dp
                ) {
                    Text(
                        text = "After",
                        modifier = Modifier.padding(
                            horizontal = Spacing.small,
                            vertical = Spacing.extraSmall
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
