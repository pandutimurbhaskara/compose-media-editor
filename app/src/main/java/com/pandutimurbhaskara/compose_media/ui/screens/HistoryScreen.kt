package com.pandutimurbhaskara.compose_media.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandutimurbhaskara.compose_media.ui.theme.ComposemediaTheme
import com.pandutimurbhaskara.compose_media.ui.theme.Dimensions
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing
import com.pandutimurbhaskara.compose_media.ui.theme.StatusCompleted
import com.pandutimurbhaskara.compose_media.ui.theme.StatusDraft
import com.pandutimurbhaskara.compose_media.ui.theme.StatusExported
import com.pandutimurbhaskara.compose_media.ui.theme.StatusFailed

/**
 * History screen with recent edits list
 */
@Composable
fun HistoryScreen(
	modifier: Modifier = Modifier,
	onProjectClick: (HistoryProject) -> Unit = {},
	onMoreOptionsClick: (HistoryProject) -> Unit = {},
	onStartEditingClick: () -> Unit = {}
) {
	// Filter state
	var selectedFilter by rememberSaveable { mutableStateOf(ProjectFilter.ALL) }

	Column(
		modifier = modifier.fillMaxSize()
	) {
		// Header
		HistoryHeader()

		// Filter Chips
		FilterChipRow(
			selectedFilter = selectedFilter,
			onFilterSelected = { selectedFilter = it },
			modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
		)

		// Project List or Empty State
		val filteredProjects = sampleProjects.filter { project ->
			when (selectedFilter) {
				ProjectFilter.ALL -> true
				ProjectFilter.DRAFTS -> project.status == ProjectStatus.DRAFT
				ProjectFilter.COMPLETED -> project.status == ProjectStatus.COMPLETED
				ProjectFilter.EXPORTED -> project.status == ProjectStatus.EXPORTED
			}
		}

		if (filteredProjects.isEmpty()) {
			EmptyState(
				onStartEditingClick = onStartEditingClick,
				modifier = Modifier.fillMaxSize()
			)
		} else {
			ProjectList(
				projects = filteredProjects,
				onProjectClick = onProjectClick,
				onMoreOptionsClick = onMoreOptionsClick,
				modifier = Modifier.fillMaxSize()
			)
		}
	}
}

/**
 * History screen header with "Recent Edits" title
 */
@Composable
private fun HistoryHeader(
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surface
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(Spacing.medium),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = "Recent Edits",
				style = MaterialTheme.typography.headlineSmall.copy(
					fontSize = 24.sp
				),
				color = MaterialTheme.colorScheme.onSurface
			)

			IconButton(onClick = { /* Filter options */ }) {
				Icon(
					imageVector = Icons.Default.Settings,
					contentDescription = "Filter",
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

/**
 * Horizontal scrolling filter chips
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipRow(
	selectedFilter: ProjectFilter,
	onFilterSelected: (ProjectFilter) -> Unit,
	modifier: Modifier = Modifier
) {
	LazyRow(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(Spacing.small),
		contentPadding = PaddingValues(horizontal = Spacing.small)
	) {
		items(ProjectFilter.values()) { filter ->
			FilterChip(
				selected = selectedFilter == filter,
				onClick = { onFilterSelected(filter) },
				label = {
					Text(
						text = filter.label,
						style = MaterialTheme.typography.labelLarge
					)
				},
				colors = FilterChipDefaults.filterChipColors(
					selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
					selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
				)
			)
		}
	}
}

/**
 * Scrollable list of project cards
 */
@Composable
private fun ProjectList(
	projects: List<HistoryProject>,
	onProjectClick: (HistoryProject) -> Unit,
	onMoreOptionsClick: (HistoryProject) -> Unit,
	modifier: Modifier = Modifier
) {
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(Spacing.medium),
		verticalArrangement = Arrangement.spacedBy(Spacing.medium)
	) {
		items(projects) { project ->
			ProjectCard(
				project = project,
				onClick = { onProjectClick(project) },
				onMoreOptionsClick = { onMoreOptionsClick(project) }
			)
		}
	}
}

/**
 * Project card component with thumbnail, details, and status badge
 */
@Composable
private fun ProjectCard(
	project: HistoryProject,
	onClick: () -> Unit,
	onMoreOptionsClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Card(
		modifier = modifier
			.fillMaxWidth()
			.clickable(
				onClick = onClick,
				indication = ripple(bounded = true),
				interactionSource = remember { MutableInteractionSource() }
			),
		shape = RoundedCornerShape(Spacing.medium),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant
		),
		elevation = CardDefaults.cardElevation(
			defaultElevation = 2.dp
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(Spacing.medium),
			horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
		) {
			// Thumbnail
			ProjectThumbnail(
				modifier = Modifier.size(Dimensions.thumbnailMedium)
			)

			// Content
			Column(
				modifier = Modifier
					.weight(1f)
					.align(Alignment.CenterVertically),
				verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
			) {
				// Filename
				Text(
					text = project.filename,
					style = MaterialTheme.typography.bodyLarge.copy(
						fontWeight = FontWeight.Bold,
						fontSize = 16.sp
					),
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)

				// Status Badge
				StatusBadge(status = project.status)

				// Timestamp
				Text(
					text = project.timestamp,
					style = MaterialTheme.typography.bodySmall.copy(
						fontSize = 12.sp
					),
					color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
				)
			}

			// More Options Icon
			IconButton(
				onClick = onMoreOptionsClick,
				modifier = Modifier.align(Alignment.Top)
			) {
				Icon(
					imageVector = Icons.Default.MoreVert,
					contentDescription = "More options",
					tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
				)
			}
		}
	}
}

/**
 * Project thumbnail placeholder
 */
@Composable
private fun ProjectThumbnail(
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.clip(RoundedCornerShape(Spacing.small))
			.background(MaterialTheme.colorScheme.primaryContainer),
		contentAlignment = Alignment.Center
	) {
		Icon(
			imageVector = Icons.Default.Info,
			contentDescription = "Thumbnail",
			tint = MaterialTheme.colorScheme.onPrimaryContainer,
			modifier = Modifier.size(40.dp)
		)
	}
}

/**
 * Status badge with colored background
 */
@Composable
private fun StatusBadge(
	status: ProjectStatus,
	modifier: Modifier = Modifier
) {
	val backgroundColor = when (status) {
		ProjectStatus.DRAFT -> StatusDraft
		ProjectStatus.COMPLETED -> StatusCompleted
		ProjectStatus.EXPORTED -> StatusExported
		ProjectStatus.FAILED -> StatusFailed
	}

	Surface(
		modifier = modifier,
		shape = RoundedCornerShape(12.dp),
		color = backgroundColor
	) {
		Text(
			text = status.label,
			style = MaterialTheme.typography.labelSmall.copy(
				fontSize = 12.sp,
				fontWeight = FontWeight.Medium
			),
			color = Color.White,
			modifier = Modifier.padding(horizontal = Spacing.small, vertical = 4.dp)
		)
	}
}

/**
 * Empty state when no projects exist
 */
@Composable
private fun EmptyState(
	onStartEditingClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier,
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(Spacing.medium)
		) {
			Icon(
				imageVector = Icons.Default.Info,
				contentDescription = null,
				modifier = Modifier.size(80.dp),
				tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
			)

			Text(
				text = "No edits yet",
				style = MaterialTheme.typography.titleLarge,
				color = MaterialTheme.colorScheme.onSurface
			)

			Text(
				text = "Start editing to see your history here",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			Spacer(modifier = Modifier.height(Spacing.small))

			Button(
				onClick = onStartEditingClick,
				shape = RoundedCornerShape(Spacing.medium)
			) {
				Icon(
					imageVector = Icons.Default.Add,
					contentDescription = null,
					modifier = Modifier.size(Dimensions.iconSmall)
				)
				Spacer(modifier = Modifier.width(Spacing.small))
				Text("Start Editing")
			}
		}
	}
}

/**
 * Project status enum
 */
enum class ProjectStatus(val label: String) {
	DRAFT("Draft"),
	COMPLETED("Completed"),
	EXPORTED("Exported"),
	FAILED("Failed")
}

/**
 * Filter options enum
 */
enum class ProjectFilter(val label: String) {
	ALL("All"),
	DRAFTS("Drafts"),
	COMPLETED("Completed"),
	EXPORTED("Exported")
}

/**
 * Data class for history projects
 */
data class HistoryProject(
	val id: Int,
	val filename: String,
	val status: ProjectStatus,
	val timestamp: String
)

// Sample data
private val sampleProjects = listOf(
	HistoryProject(
		id = 1,
		filename = "IMG_4502_blurred.jpg",
		status = ProjectStatus.DRAFT,
		timestamp = "2 hours ago"
	),
	HistoryProject(
		id = 2,
		filename = "video_conference_censored.mp4",
		status = ProjectStatus.COMPLETED,
		timestamp = "5 hours ago"
	),
	HistoryProject(
		id = 3,
		filename = "family_photo_edited.jpg",
		status = ProjectStatus.EXPORTED,
		timestamp = "Yesterday"
	),
	HistoryProject(
		id = 4,
		filename = "license_plate_blur.jpg",
		status = ProjectStatus.COMPLETED,
		timestamp = "2 days ago"
	),
	HistoryProject(
		id = 5,
		filename = "id_card_redacted.jpg",
		status = ProjectStatus.EXPORTED,
		timestamp = "3 days ago"
	),
	HistoryProject(
		id = 6,
		filename = "street_view_privacy.mp4",
		status = ProjectStatus.DRAFT,
		timestamp = "1 week ago"
	)
)

// Preview composables
@Preview(name = "History Screen - Light", showBackground = true)
@Composable
private fun HistoryScreenPreviewLight() {
	ComposemediaTheme(darkTheme = false) {
		HistoryScreen()
	}
}

@Preview(name = "History Screen - Dark", showBackground = true)
@Composable
private fun HistoryScreenPreviewDark() {
	ComposemediaTheme(darkTheme = true) {
		HistoryScreen()
	}
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun EmptyStatePreview() {
	ComposemediaTheme {
		EmptyState(onStartEditingClick = {})
	}
}

@Preview(name = "Project Card", showBackground = true)
@Composable
private fun ProjectCardPreview() {
	ComposemediaTheme {
		ProjectCard(
			project = sampleProjects[0],
			onClick = {},
			onMoreOptionsClick = {},
			modifier = Modifier.padding(Spacing.medium)
		)
	}
}
