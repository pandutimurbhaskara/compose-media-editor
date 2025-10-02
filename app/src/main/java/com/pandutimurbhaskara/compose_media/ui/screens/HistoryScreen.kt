package com.pandutimurbhaskara.compose_media.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pandutimurbhaskara.compose_media.ui.theme.ComposemediaTheme
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing

/**
 * History screen - placeholder for Part 4 implementation
 */
@Composable
fun HistoryScreen(
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier
			.fillMaxSize()
			.padding(Spacing.medium),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = "History Screen",
			style = MaterialTheme.typography.headlineSmall,
			color = MaterialTheme.colorScheme.onSurface
		)
	}
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
	ComposemediaTheme {
		HistoryScreen()
	}
}
