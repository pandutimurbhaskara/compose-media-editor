package com.pandutimurbhaskara.compose_media.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandutimurbhaskara.compose_media.ui.theme.ComposemediaTheme
import com.pandutimurbhaskara.compose_media.ui.theme.Dimensions
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing

/**
 * Home screen with Privacy Editor header and expandable accordions
 */
@Composable
fun HomeScreen(
	modifier: Modifier = Modifier,
	onEditingOptionClick: (EditingOption) -> Unit = {}
) {
	// Track which accordion is expanded (only one at a time)
	var expandedAccordion by rememberSaveable { mutableStateOf<String?>(null) }

	Column(
		modifier = modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
	) {
		// Header
		HomeHeader()

		// Photo Editing Accordion
		EditingAccordion(
			title = "Photo Editing",
			isExpanded = expandedAccordion == "photo",
			onToggle = {
				expandedAccordion = if (expandedAccordion == "photo") null else "photo"
			},
			options = photoEditingOptions,
			onOptionClick = onEditingOptionClick,
			modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
		)

		// Video Editing Accordion
		EditingAccordion(
			title = "Video Editing",
			isExpanded = expandedAccordion == "video",
			onToggle = {
				expandedAccordion = if (expandedAccordion == "video") null else "video"
			},
			options = videoEditingOptions,
			onOptionClick = onEditingOptionClick,
			modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
		)
	}
}

/**
 * Home screen header with "Privacy Editor" title
 */
@Composable
private fun HomeHeader(
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surface
	) {
		Text(
			text = "Privacy Editor",
			style = MaterialTheme.typography.headlineSmall.copy(
				fontSize = 24.sp
			),
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(Spacing.medium)
		)
	}
}

/**
 * Expandable accordion component for editing categories
 */
@Composable
private fun EditingAccordion(
	title: String,
	isExpanded: Boolean,
	onToggle: () -> Unit,
	options: List<EditingOption>,
	onOptionClick: (EditingOption) -> Unit,
	modifier: Modifier = Modifier
) {
	// Animated rotation for chevron icon
	val chevronRotation by animateFloatAsState(
		targetValue = if (isExpanded) 180f else 0f,
		animationSpec = tween(durationMillis = 300),
		label = "chevronRotation"
	)

	Card(
		modifier = modifier.fillMaxWidth(),
		shape = RoundedCornerShape(Spacing.medium),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 2.dp
		)
	) {
		Column {
			// Accordion Header
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.clickable(
						onClick = onToggle,
						indication = ripple(bounded = true),
						interactionSource = remember { MutableInteractionSource() }
					)
					.padding(Spacing.medium),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = title,
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.weight(1f)
				)

				Icon(
					imageVector = Icons.Default.KeyboardArrowDown,
					contentDescription = if (isExpanded) "Collapse" else "Expand",
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier
						.size(Dimensions.iconMedium)
						.rotate(chevronRotation)
				)
			}

			// Accordion Content with Animation
			AnimatedVisibility(
				visible = isExpanded,
				enter = fadeIn(animationSpec = tween(300)) +
						expandVertically(animationSpec = tween(300)),
				exit = fadeOut(animationSpec = tween(300)) +
						shrinkVertically(animationSpec = tween(300))
			) {
				Column {
					options.forEachIndexed { index, option ->
						if (index > 0) {
							HorizontalDivider(
								color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
								thickness = 1.dp
							)
						}
						EditingOptionItem(
							option = option,
							onClick = { onOptionClick(option) }
						)
					}
				}
			}
		}
	}
}

/**
 * Individual editing option item with icon, text, and chevron
 */
@Composable
private fun EditingOptionItem(
	option: EditingOption,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.clickable(
				onClick = onClick,
				indication = ripple(bounded = true),
				interactionSource = remember { MutableInteractionSource() }
			)
			.padding(horizontal = Spacing.medium, vertical = Spacing.medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		// Option Icon
		Icon(
			imageVector = option.icon,
			contentDescription = option.label,
			tint = MaterialTheme.colorScheme.primary,
			modifier = Modifier.size(Dimensions.iconMedium)
		)

		// Option Label
		Text(
			text = option.label,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier
				.weight(1f)
				.padding(start = Spacing.medium)
		)

		// Chevron Icon
		Icon(
			imageVector = Icons.AutoMirrored.Filled.ArrowForward,
			contentDescription = "Open",
			tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
			modifier = Modifier.size(Dimensions.iconMedium)
		)
	}
}

/**
 * Data class for editing options
 */
data class EditingOption(
	val id: String,
	val label: String,
	val icon: ImageVector,
	val type: EditingType
)

enum class EditingType {
	PHOTO, VIDEO
}

// Photo editing options
private val photoEditingOptions = listOf(
	EditingOption(
		id = "photo_blur_face",
		label = "Blur Face",
		icon = Icons.Default.Face,
		type = EditingType.PHOTO
	),
	EditingOption(
		id = "photo_blur_plate",
		label = "Blur License Plate",
		icon = Icons.Default.Check,
		type = EditingType.PHOTO
	),
	EditingOption(
		id = "photo_blur_id",
		label = "Blur ID Card",
		icon = Icons.Default.AccountBox,
		type = EditingType.PHOTO
	),
	EditingOption(
		id = "photo_custom_blur",
		label = "Custom Blur Area",
		icon = Icons.Default.Check,
		type = EditingType.PHOTO
	)
)

// Video editing options
private val videoEditingOptions = listOf(
	EditingOption(
		id = "video_blur_face",
		label = "Blur Face",
		icon = Icons.Default.Face,
		type = EditingType.VIDEO
	),
	EditingOption(
		id = "video_blur_plate",
		label = "Blur License Plate",
		icon = Icons.Default.Check, // Using Check as placeholder for car icon
		type = EditingType.VIDEO
	),
	EditingOption(
		id = "video_blur_id",
		label = "Blur ID Card",
		icon = Icons.Default.AccountBox,
		type = EditingType.VIDEO
	),
	EditingOption(
		id = "video_custom_blur",
		label = "Custom Blur Area",
		icon = Icons.Default.Check, // Using Check as placeholder for crop icon
		type = EditingType.VIDEO
	)
)

// Preview composables
@Preview(name = "Home Screen - Light", showBackground = true)
@Composable
private fun HomeScreenPreviewLight() {
	ComposemediaTheme(darkTheme = false) {
		HomeScreen()
	}
}

@Preview(name = "Home Screen - Dark", showBackground = true)
@Composable
private fun HomeScreenPreviewDark() {
	ComposemediaTheme(darkTheme = true) {
		HomeScreen()
	}
}
