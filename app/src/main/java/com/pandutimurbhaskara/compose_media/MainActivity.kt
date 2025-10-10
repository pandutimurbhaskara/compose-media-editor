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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pandutimurbhaskara.compose_media.navigation.AppNavGraph
import com.pandutimurbhaskara.compose_media.navigation.Routes
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
	val navController = rememberNavController()
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val currentDestination = navBackStackEntry?.destination

	// Navigation items with string resources
	val navItems = listOf(
		NavBarItem(
			label = stringResource(R.string.nav_home),
			icon = Icons.Default.Home,
			route = Routes.HOME
		),
		NavBarItem(
			label = stringResource(R.string.nav_history),
			icon = Icons.Default.DateRange,
			route = Routes.HISTORY
		),
		NavBarItem(
			label = stringResource(R.string.nav_settings),
			icon = Icons.Default.Settings,
			route = Routes.SETTINGS
		)
	)

	// Determine if bottom bar should be shown
	val showBottomBar = currentDestination?.route in listOf(
		Routes.HOME,
		Routes.HISTORY,
		Routes.SETTINGS
	)

	// Snackbar state
	val snackbarHostState = remember { SnackbarHostState() }
	val scope = rememberCoroutineScope()

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
			if (showBottomBar) {
				BottomNavBar(
					items = navItems,
					selectedItem = navItems.find { item ->
						currentDestination?.hierarchy?.any { it.route == item.route } == true
					} ?: navItems[0],
					onItemSelected = { item ->
						navController.navigate(item.route) {
							// Pop up to the start destination of the graph to
							// avoid building up a large stack of destinations
							popUpTo(navController.graph.findStartDestination().id) {
								saveState = true
							}
							// Avoid multiple copies of the same destination when
							// reselecting the same item
							launchSingleTop = true
							// Restore state when reselecting a previously selected item
							restoreState = true
						}
					}
				)
			}
		}
	) { innerPadding ->
		// Navigation graph handles its own padding per screen
		AppNavGraph(
			navController = navController,
			startDestination = Routes.HOME
		)
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