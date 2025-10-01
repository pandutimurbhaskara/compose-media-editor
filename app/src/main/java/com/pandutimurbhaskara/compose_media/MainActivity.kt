package com.pandutimurbhaskara.compose_media

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.pandutimurbhaskara.compose_media.ui.theme.ComposemediaTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			ComposemediaTheme {
				ComposemediaApp()
			}
		}
	}
}

@PreviewScreenSizes
@Composable
fun ComposemediaApp() {
	var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

	NavigationSuiteScaffold(
		navigationSuiteItems = {
			AppDestinations.entries.forEach {
				item(
					icon = {
						Icon(
							it.icon,
							contentDescription = it.label
						)
					},
					label = { Text(it.label) },
					selected = it == currentDestination,
					onClick = { currentDestination = it }
				)
			}
		}
	) {
		Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding),
				contentAlignment = Alignment.Center
			) {
				when (currentDestination) {
					AppDestinations.HOME -> Text(
						text = "${currentDestination.label} Screen",
						style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
					)
					AppDestinations.HISTORY -> Text(
						text = "${currentDestination.label} Screen",
						style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
					)
					AppDestinations.SETTINGS -> Text(
						text = "${currentDestination.label} Screen",
						style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
					)
				}
			}
		}
	}
}

enum class AppDestinations(
	val label: String,
	val icon: ImageVector,
) {
	HOME("Home", Icons.Default.Home),
	HISTORY("History", Icons.Default.DateRange),
	SETTINGS("Setting", Icons.Default.Settings),
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