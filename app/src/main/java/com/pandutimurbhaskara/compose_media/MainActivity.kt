package com.pandutimurbhaskara.compose_media

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.pandutimurbhaskara.compose_media.ui.navigation.BottomNavBar
import com.pandutimurbhaskara.compose_media.ui.navigation.NavBarItem
import com.pandutimurbhaskara.compose_media.ui.screens.HistoryScreen
import com.pandutimurbhaskara.compose_media.ui.screens.HomeScreen
import com.pandutimurbhaskara.compose_media.ui.screens.SettingsScreen
import com.pandutimurbhaskara.compose_media.ui.theme.ComposemediaTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
	private lateinit var appPrefs: AppPreferences

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		appPrefs = AppPreferences(this)

		// Apply saved language
		setAppLocale(this, appPrefs.getLanguage())

		enableEdgeToEdge()
		setContent {
			val context = LocalContext.current
			val prefs = remember { AppPreferences(context) }

			// Theme state
			var themeMode by remember { mutableIntStateOf(prefs.getThemeMode()) }

			// Determine dark theme based on theme mode
			val darkTheme = when (themeMode) {
				AppPreferences.THEME_LIGHT -> false
				AppPreferences.THEME_DARK -> true
				else -> isSystemInDarkTheme()
			}

			ComposemediaTheme(darkTheme = darkTheme) {
				ComposemediaApp(
					onThemeChanged = { newTheme ->
						themeMode = newTheme
						prefs.setThemeMode(newTheme)
					},
					onLanguageChanged = { newLanguage ->
						prefs.setLanguage(newLanguage)
						setAppLocale(context, newLanguage)
						// Recreate activity to apply new language
						recreate()
					}
				)
			}
		}
	}

	/**
	 * Set app locale
	 */
	private fun setAppLocale(context: Context, languageCode: String) {
		val locale = Locale(languageCode)
		Locale.setDefault(locale)

		val config = Configuration(context.resources.configuration)
		config.setLocale(locale)
		context.createConfigurationContext(config)
		context.resources.updateConfiguration(config, context.resources.displayMetrics)
	}
}

@Composable
fun ComposemediaApp(
	onThemeChanged: (Int) -> Unit = {},
	onLanguageChanged: (String) -> Unit = {}
) {
	// Navigation items with string resources
	val navItems = listOf(
		NavBarItem(
			label = stringResource(R.string.nav_home),
			icon = Icons.Default.Home,
			route = "home"
		),
		NavBarItem(
			label = stringResource(R.string.nav_history),
			icon = Icons.Default.DateRange,
			route = "history"
		),
		NavBarItem(
			label = stringResource(R.string.nav_settings),
			icon = Icons.Default.Settings,
			route = "settings"
		)
	)

	// Store index instead of the entire NavBarItem (which contains non-saveable ImageVector)
	var selectedIndex by rememberSaveable { mutableStateOf(0) }
	val selectedItem = navItems[selectedIndex]

	// Snackbar state
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()

	// Navigation helper function
	val navigateToTab = { route: String ->
		val index = navItems.indexOfFirst { it.route == route }
		if (index >= 0) {
			selectedIndex = index
		}
	}

	// Show snackbar helper function
	val showSnackbar: (String) -> Unit = { message ->
		scope.launch {
			snackbarHostState.showSnackbar(message)
		}
		Unit
	}

	Scaffold(
		modifier = Modifier.fillMaxSize(),
		snackbarHost = {
			SnackbarHost(hostState = snackbarHostState)
		},
		bottomBar = {
			BottomNavBar(
				items = navItems,
				selectedItem = selectedItem,
				onItemSelected = { item ->
					selectedIndex = navItems.indexOf(item)
				}
			)
		}
	) { innerPadding ->
		// Screen content based on selected item
		when (selectedItem.route) {
			"home" -> {
				val context = LocalContext.current
				HomeScreen(
					modifier = Modifier
						.fillMaxSize()
						.padding(innerPadding),
					onEditingOptionClick = { option ->
						// Show snackbar with option label
						val message = context.getString(
							when (option.type) {
								com.pandutimurbhaskara.compose_media.ui.screens.EditingType.PHOTO ->
									R.string.snackbar_opening_photo_editor
								com.pandutimurbhaskara.compose_media.ui.screens.EditingType.VIDEO ->
									R.string.snackbar_opening_video_editor
							}
						)
						showSnackbar(message)
						// Navigate to history after a short delay
						navigateToTab("history")
					}
				)
			}
			"history" -> HistoryScreen(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding),
				onStartEditingClick = {
					// Navigate to home when start editing is clicked
					navigateToTab("home")
				}
			)
			"settings" -> SettingsScreen(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding),
				onThemeChanged = onThemeChanged,
				onLanguageChanged = onLanguageChanged,
				onShowSnackbar = showSnackbar
			)
		}
	}
}

@Preview(showBackground = true, name = "Light Theme")
@Composable
fun AppPreviewLight() {
	ComposemediaTheme(darkTheme = false) {
		ComposemediaApp()
	}
}

@Preview(showBackground = true, name = "Dark Theme")
@Composable
fun AppPreviewDark() {
	ComposemediaTheme(darkTheme = true) {
		ComposemediaApp()
	}
}