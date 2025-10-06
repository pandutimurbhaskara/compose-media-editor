package com.pandutimurbhaskara.compose_media.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pandutimurbhaskara.compose_media.R
import com.pandutimurbhaskara.compose_media.ui.theme.ComposemediaTheme
import com.pandutimurbhaskara.compose_media.ui.theme.Dimensions
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing
import com.pandutimurbhaskara.compose_media.util.ImagePickerHelper
import kotlinx.coroutines.launch

/**
 * Screen for selecting images from gallery or camera
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    onImageSelected: (Uri) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imagePickerHelper = remember { ImagePickerHelper(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for camera photo URI
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                onImageSelected(uri)
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                onImageSelected(uri)
            }
        }
    }

    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, launch camera
            launchCamera(imagePickerHelper, cameraLauncher) { uri ->
                photoUri = uri
            }
        } else {
            // Permission denied
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required to take photos")
            }
        }
    }

    // Media permission launcher (Android 13+)
    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Permission granted, launch gallery
            galleryLauncher.launch(imagePickerHelper.createGalleryIntent())
        } else {
            // Permission denied
            scope.launch {
                snackbarHostState.showSnackbar("Media permission is required to access gallery")
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Select Image") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gallery button
            Button(
                onClick = {
                    // Check and request permission if needed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Android 13+ requires READ_MEDIA_IMAGES
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_MEDIA_IMAGES
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                galleryLauncher.launch(imagePickerHelper.createGalleryIntent())
                            }
                            else -> {
                                mediaPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }
                        }
                    } else {
                        // Android 12 and below
                        galleryLauncher.launch(imagePickerHelper.createGalleryIntent())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Spacer(Modifier.width(Spacing.small))
                Text("Select from Gallery")
            }

            Spacer(Modifier.height(Spacing.medium))

            // Camera button
            OutlinedButton(
                onClick = {
                    // Check camera permission
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            // Permission granted, launch camera
                            launchCamera(imagePickerHelper, cameraLauncher) { uri ->
                                photoUri = uri
                            }
                        }
                        else -> {
                            // Request permission
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconMedium)
                )
                Spacer(Modifier.width(Spacing.small))
                Text("Take Photo")
            }

            Spacer(Modifier.height(Spacing.large))

            // Info text
            Text(
                text = "Select an image to start editing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Helper function to launch camera
 */
private fun launchCamera(
    imagePickerHelper: ImagePickerHelper,
    cameraLauncher: androidx.activity.compose.ManagedActivityResultLauncher<android.content.Intent, androidx.activity.result.ActivityResult>,
    onUriCreated: (Uri) -> Unit
) {
    try {
        val file = imagePickerHelper.createImageFile()
        val uri = imagePickerHelper.getUriForFile(file)
        onUriCreated(uri)
        cameraLauncher.launch(imagePickerHelper.createCameraIntent(uri))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Preview
@Preview(showBackground = true, name = "Image Selection Screen - Light")
@Composable
private fun ImageSelectionScreenPreviewLight() {
    ComposemediaTheme(darkTheme = false) {
        ImageSelectionScreen(
            onImageSelected = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Image Selection Screen - Dark")
@Composable
private fun ImageSelectionScreenPreviewDark() {
    ComposemediaTheme(darkTheme = true) {
        ImageSelectionScreen(
            onImageSelected = {},
            onNavigateBack = {}
        )
    }
}
