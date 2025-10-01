package com.pandutimurbhaskara.compose_media.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandutimurbhaskara.compose_media.ui.theme.ComposemediaTheme
import com.pandutimurbhaskara.compose_media.ui.theme.Dimensions
import com.pandutimurbhaskara.compose_media.ui.theme.Elevation
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing

/**
 * Material 3 Bottom Navigation Bar for PrivacyEdit app
 *
 * Specifications:
 * - Height: 80dp
 * - Icons: 24dp
 * - Label typography: 12sp
 * - Active state: Indigo background pill
 * - Inactive state: Gray icons + text
 * - Smooth transitions and ripple effects
 */
@Composable
fun BottomNavBar(
	items: List<NavBarItem>,
	selectedItem: NavBarItem,
	onItemSelected: (NavBarItem) -> Unit,
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier,
		color = MaterialTheme.colorScheme.surface,
		tonalElevation = Elevation.level2,
		shadowElevation = Elevation.level3
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(Dimensions.bottomNavHeight)
				.padding(horizontal = Spacing.small),
			horizontalArrangement = Arrangement.SpaceEvenly,
			verticalAlignment = Alignment.CenterVertically
		) {
			items.forEach { item ->
				NavItem(
					icon = item.icon,
					label = item.label,
					selected = item == selectedItem,
					onClick = { onItemSelected(item) },
					modifier = Modifier.weight(1f)
				)
			}
		}
	}
}

/**
 * Individual navigation item with Material 3 styling
 */
@Composable
private fun NavItem(
	icon: ImageVector,
	label: String,
	selected: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	val interactionSource = remember { MutableInteractionSource() }

	// Animated colors and scale
	val iconColor by animateColorAsState(
		targetValue = if (selected) {
			MaterialTheme.colorScheme.onPrimaryContainer
		} else {
			MaterialTheme.colorScheme.onSurfaceVariant
		},
		animationSpec = tween(durationMillis = 200),
		label = "iconColor"
	)

	val textColor by animateColorAsState(
		targetValue = if (selected) {
			MaterialTheme.colorScheme.onSurface
		} else {
			MaterialTheme.colorScheme.onSurfaceVariant
		},
		animationSpec = tween(durationMillis = 200),
		label = "textColor"
	)

	val indicatorColor by animateColorAsState(
		targetValue = if (selected) {
			MaterialTheme.colorScheme.primaryContainer
		} else {
			Color.Transparent
		},
		animationSpec = tween(durationMillis = 300),
		label = "indicatorColor"
	)

	val scale by animateFloatAsState(
		targetValue = if (selected) 1.0f else 0.95f,
		animationSpec = tween(durationMillis = 200),
		label = "scale"
	)

	Column(
		modifier = modifier
			.clip(RoundedCornerShape(Spacing.medium))
			.clickable(
				onClick = onClick,
				interactionSource = interactionSource,
				indication = ripple(
					bounded = true,
					color = MaterialTheme.colorScheme.primary
				)
			)
			.padding(vertical = Spacing.small),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		// Icon container with background pill for active state
		Box(
			modifier = Modifier
				.height(32.dp)
				.clip(RoundedCornerShape(16.dp))
				.background(indicatorColor)
				.padding(horizontal = Spacing.medium)
				.scale(scale),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				modifier = Modifier.size(Dimensions.bottomNavIconSize),
				tint = iconColor
			)
		}

		// Label
		Text(
			text = label,
			style = MaterialTheme.typography.labelMedium.copy(
				fontSize = 12.sp
			),
			color = textColor,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier.padding(top = Spacing.extraSmall)
		)
	}
}

/**
 * Data class for navigation bar items
 */
data class NavBarItem(
	val label: String,
	val icon: ImageVector,
	val route: String
)

// Preview composables
@Preview(name = "Bottom Nav Bar - Light", showBackground = true)
@Composable
private fun BottomNavBarPreviewLight() {
	ComposemediaTheme(darkTheme = false) {
		val items = listOf(
			NavBarItem("Home", Icons.Default.Home, "home"),
			NavBarItem("History", Icons.Default.DateRange, "history"),
			NavBarItem("Setting", Icons.Default.Settings, "settings")
		)
		BottomNavBar(
			items = items,
			selectedItem = items[0],
			onItemSelected = {}
		)
	}
}

@Preview(name = "Bottom Nav Bar - Dark", showBackground = true)
@Composable
private fun BottomNavBarPreviewDark() {
	ComposemediaTheme(darkTheme = true) {
		val items = listOf(
			NavBarItem("Home", Icons.Default.Home, "home"),
			NavBarItem("History", Icons.Default.DateRange, "history"),
			NavBarItem("Setting", Icons.Default.Settings, "settings")
		)
		BottomNavBar(
			items = items,
			selectedItem = items[1],
			onItemSelected = {}
		)
	}
}
