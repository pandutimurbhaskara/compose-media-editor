package com.pandutimurbhaskara.compose_media.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pandutimurbhaskara.compose_media.ui.screens.FaceBlurEditorScreen
import com.pandutimurbhaskara.compose_media.ui.screens.HistoryScreen
import com.pandutimurbhaskara.compose_media.ui.screens.HomeScreen
import com.pandutimurbhaskara.compose_media.ui.screens.IdCardEditorScreen
import com.pandutimurbhaskara.compose_media.ui.screens.ImageSelectionScreen
import com.pandutimurbhaskara.compose_media.ui.screens.LicensePlateEditorScreen
import com.pandutimurbhaskara.compose_media.ui.screens.SettingsScreen

/**
 * Navigation routes for the app
 */
object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val IMAGE_SELECTION = "image_selection/{mode}"
    const val FACE_BLUR_EDITOR = "face_blur_editor/{imageUri}"
    const val ID_CARD_EDITOR = "id_card_editor/{imageUri}"
    const val LICENSE_PLATE_EDITOR = "license_plate_editor/{imageUri}"

    fun imageSelection(mode: String) = "image_selection/$mode"
    fun faceBlurEditor(imageUri: String) = "face_blur_editor/$imageUri"
    fun idCardEditor(imageUri: String) = "id_card_editor/$imageUri"
    fun licensePlateEditor(imageUri: String) = "license_plate_editor/$imageUri"
}

/**
 * Editing modes for navigation
 */
enum class EditingMode {
    FACE_BLUR,
    ID_CARD_BLUR,
    LICENSE_PLATE_BLUR,
    CUSTOM_BLUR
}

/**
 * Main navigation graph for the app
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.HOME,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home Screen
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToHistory = {
                    navController.navigate(Routes.HISTORY)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onEditingOptionClick = { optionId ->
                    when (optionId) {
                        "photo_blur_face" -> {
                            navController.navigate(Routes.imageSelection(EditingMode.FACE_BLUR.name))
                        }
                        "photo_blur_id" -> {
                            navController.navigate(Routes.imageSelection(EditingMode.ID_CARD_BLUR.name))
                        }
                        "photo_blur_plate" -> {
                            navController.navigate(Routes.imageSelection(EditingMode.LICENSE_PLATE_BLUR.name))
                        }
                        "photo_custom_blur" -> {
                            navController.navigate(Routes.imageSelection(EditingMode.CUSTOM_BLUR.name))
                        }
                    }
                }
            )
        }

        // History Screen
        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings Screen
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Image Selection Screen
        composable(
            route = Routes.IMAGE_SELECTION,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode")?.let {
                try {
                    EditingMode.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    EditingMode.FACE_BLUR
                }
            } ?: EditingMode.FACE_BLUR

            ImageSelectionScreen(
                onImageSelected = { uri ->
                    val encodedUri = Uri.encode(uri.toString())
                    when (mode) {
                        EditingMode.FACE_BLUR -> {
                            navController.navigate(Routes.faceBlurEditor(encodedUri))
                        }
                        EditingMode.ID_CARD_BLUR -> {
                            navController.navigate(Routes.idCardEditor(encodedUri))
                        }
                        EditingMode.LICENSE_PLATE_BLUR -> {
                            navController.navigate(Routes.licensePlateEditor(encodedUri))
                        }
                        EditingMode.CUSTOM_BLUR -> {
                            // For now, use face blur editor with manual editing
                            navController.navigate(Routes.faceBlurEditor(encodedUri))
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Face Blur Editor
        composable(
            route = Routes.FACE_BLUR_EDITOR,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri")
            val imageUri = encodedUri?.let { Uri.parse(Uri.decode(it)) }

            if (imageUri != null) {
                FaceBlurEditorScreen(
                    imageUri = imageUri,
                    onSave = { bitmap ->
                        // TODO: Navigate to preview or save directly
                        // For now, navigate back to home
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // ID Card Editor
        composable(
            route = Routes.ID_CARD_EDITOR,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri")
            val imageUri = encodedUri?.let { Uri.parse(Uri.decode(it)) }

            if (imageUri != null) {
                IdCardEditorScreen(
                    imageUri = imageUri,
                    onSave = { bitmap ->
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // License Plate Editor
        composable(
            route = Routes.LICENSE_PLATE_EDITOR,
            arguments = listOf(
                navArgument("imageUri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri")
            val imageUri = encodedUri?.let { Uri.parse(Uri.decode(it)) }

            if (imageUri != null) {
                LicensePlateEditorScreen(
                    imageUri = imageUri,
                    onSave = { bitmap ->
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
