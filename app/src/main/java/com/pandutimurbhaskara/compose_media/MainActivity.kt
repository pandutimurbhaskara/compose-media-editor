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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pandutimurbhaskara.compose_media.ui.navigation.BottomNavBar
import com.pandutimurbhaskara.compose_media.ui.navigation.NavBarItem
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

@Composable
fun ComposemediaApp() {
	// Navigation items
	val navItems = remember {
		listOf(
			NavBarItem(
				label = "Home",
				icon = Icons.Default.Home,
				route = "home"
			),
			NavBarItem(
				label = "History",
				icon = Icons.Default.DateRange,
				route = "history"
			),
			NavBarItem(
				label = "Setting",
				icon = Icons.Default.Settings,
				route = "settings"
			)
		)
	}

	// Store index instead of the entire NavBarItem (which contains non-saveable ImageVector)
	var selectedIndex by rememberSaveable { mutableStateOf(0) }
	val selectedItem = navItems[selectedIndex]

	Scaffold(
		modifier = Modifier.fillMaxSize(),
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
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding),
			contentAlignment = Alignment.Center
		) {
			// Screen content based on selected item
			when (selectedItem.route) {
				"home" -> Text(
					text = "Home Screen",
					style = MaterialTheme.typography.headlineSmall
				)
				"history" -> Text(
					text = "History Screen",
					style = MaterialTheme.typography.headlineSmall
				)
				"settings" -> Text(
					text = "Setting Screen",
					style = MaterialTheme.typography.headlineSmall
				)
			}
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