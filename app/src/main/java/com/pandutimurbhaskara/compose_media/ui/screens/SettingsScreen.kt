package com.pandutimurbhaskara.compose_media.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandutimurbhaskara.compose_media.AppPreferences
import com.pandutimurbhaskara.compose_media.R
import com.pandutimurbhaskara.compose_media.ui.theme.ComposemediaTheme
import com.pandutimurbhaskara.compose_media.ui.theme.Dimensions
import com.pandutimurbhaskara.compose_media.ui.theme.Spacing

/**
 * Settings screen with theme, language, and app info
 */
@Composable
fun SettingsScreen(
	modifier: Modifier = Modifier,
	onThemeChanged: (Int) -> Unit = {},
	onLanguageChanged: (String) -> Unit = {},
	onPrivacyPolicyClick: () -> Unit = {},
	onTermsClick: () -> Unit = {}
) {
	val context = LocalContext.current
	val prefs = remember { AppPreferences(context) }

	// Load saved preferences
	val savedTheme = prefs.getThemeMode()
	val savedLanguage = prefs.getLanguage()

	// Theme selection state
	var selectedTheme by remember {
		mutableStateOf(
			when (savedTheme) {
				AppPreferences.THEME_LIGHT -> ThemeMode.LIGHT
				AppPreferences.THEME_DARK -> ThemeMode.DARK
				else -> ThemeMode.SYSTEM
			}
		)
	}

	// Language selection state
	var selectedLanguage by remember {
		mutableStateOf(
			when (savedLanguage) {
				AppPreferences.LANG_INDONESIAN -> Language.INDONESIAN
				else -> Language.ENGLISH
			}
		)
	}
	var showLanguageDialog by remember { mutableStateOf(false) }

	// Clear cache dialog state
	var showClearCacheDialog by remember { mutableStateOf(false) }

	Column(
		modifier = modifier
			.fillMaxSize()
			.verticalScroll(rememberScrollState())
	) {
		// Header
		SettingsHeader()

		// Appearance Section
		SettingsSection(title = stringResource(R.string.settings_appearance)) {
			ThemeSelector(
				selectedTheme = selectedTheme,
				onThemeSelected = { newTheme ->
					selectedTheme = newTheme
					// Call the callback to actually change the theme
					val themeInt = when (newTheme) {
						ThemeMode.LIGHT -> AppPreferences.THEME_LIGHT
						ThemeMode.DARK -> AppPreferences.THEME_DARK
						ThemeMode.SYSTEM -> AppPreferences.THEME_SYSTEM
					}
					onThemeChanged(themeInt)
				}
			)
		}

		HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

		// Language & Region Section
		SettingsSection(title = stringResource(R.string.settings_language_region)) {
			SettingItem(
				icon = Icons.Default.Settings,
				title = stringResource(R.string.settings_language),
				value = selectedLanguage.displayName,
				onClick = { showLanguageDialog = true },
				showChevron = true
			)
		}

		HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

		// About Section
		SettingsSection(title = stringResource(R.string.settings_about)) {
			// App Version
			SettingItem(
				icon = Icons.Default.Phone,
				title = stringResource(R.string.settings_version),
				value = "1.0.0",
				onClick = null,
				showChevron = false
			)

			Spacer(modifier = Modifier.height(Spacing.small))

			// Storage Used
			SettingItem(
				icon = Icons.Default.Delete,
				title = stringResource(R.string.settings_storage),
				value = "42.3 MB",
				onClick = null,
				showChevron = false
			)

			Spacer(modifier = Modifier.height(Spacing.medium))

			// Clear Cache Button
			OutlinedButton(
				onClick = { showClearCacheDialog = true },
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = Spacing.medium),
				shape = RoundedCornerShape(Spacing.medium)
			) {
				Text(stringResource(R.string.settings_clear_cache))
			}

			Spacer(modifier = Modifier.height(Spacing.medium))

			// Privacy Policy
			SettingItem(
				title = stringResource(R.string.settings_privacy_policy),
				onClick = onPrivacyPolicyClick,
				showChevron = true
			)

			Spacer(modifier = Modifier.height(Spacing.small))

			// Terms of Service
			SettingItem(
				title = stringResource(R.string.settings_terms),
				onClick = onTermsClick,
				showChevron = true
			)
		}

		Spacer(modifier = Modifier.height(Spacing.extraLarge))
	}

	// Language Picker Dialog
	if (showLanguageDialog) {
		LanguagePickerDialog(
			selectedLanguage = selectedLanguage,
			onLanguageSelected = { newLanguage ->
				selectedLanguage = newLanguage
				showLanguageDialog = false
				// Call the callback to actually change the language
				val languageCode = when (newLanguage) {
					Language.INDONESIAN -> AppPreferences.LANG_INDONESIAN
					Language.ENGLISH -> AppPreferences.LANG_ENGLISH
				}
				onLanguageChanged(languageCode)
			},
			onDismiss = { showLanguageDialog = false }
		)
	}

	// Clear Cache Confirmation Dialog
	if (showClearCacheDialog) {
		ClearCacheDialog(
			onConfirm = {
				showClearCacheDialog = false
				// TODO: Implement cache clearing
			},
			onDismiss = { showClearCacheDialog = false }
		)
	}
}

/**
 * Settings screen header
 */
@Composable
private fun SettingsHeader(
	modifier: Modifier = Modifier
) {
	Surface(
		modifier = modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surface
	) {
		Text(
			text = stringResource(R.string.settings_title),
			style = MaterialTheme.typography.headlineSmall.copy(
				fontSize = 24.sp
			),
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(Spacing.medium)
		)
	}
}

/**
 * Settings section with title
 */
@Composable
private fun SettingsSection(
	title: String,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = Spacing.medium)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium.copy(
				fontWeight = FontWeight.SemiBold
			),
			color = MaterialTheme.colorScheme.primary,
			modifier = Modifier.padding(bottom = Spacing.medium)
		)
		content()
	}
}

/**
 * Theme selector with segmented buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelector(
	selectedTheme: ThemeMode,
	onThemeSelected: (ThemeMode) -> Unit,
	modifier: Modifier = Modifier
) {
	SingleChoiceSegmentedButtonRow(
		modifier = modifier.fillMaxWidth()
	) {
		ThemeMode.values().forEachIndexed { index, theme ->
			SegmentedButton(
				selected = selectedTheme == theme,
				onClick = { onThemeSelected(theme) },
				shape = SegmentedButtonDefaults.itemShape(
					index = index,
					count = ThemeMode.values().size
				),
				icon = {
					if (selectedTheme == theme) {
						SegmentedButtonDefaults.Icon(active = true) {
							Icon(
								imageVector = Icons.Default.Check,
								contentDescription = null,
								modifier = Modifier.size(Dimensions.iconSmall)
							)
						}
					} else {
						SegmentedButtonDefaults.Icon(active = false)
					}
				}
			) {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					val themeLabel = stringResource(
						when (theme) {
							ThemeMode.SYSTEM -> R.string.settings_theme_system
							ThemeMode.LIGHT -> R.string.settings_theme_light
							ThemeMode.DARK -> R.string.settings_theme_dark
						}
					)
					Icon(
						imageVector = theme.icon,
						contentDescription = themeLabel,
						modifier = Modifier.size(Dimensions.iconMedium)
					)
					Text(
						text = themeLabel,
						style = MaterialTheme.typography.labelSmall,
						modifier = Modifier.padding(top = 4.dp)
					)
				}
			}
		}
	}
}

/**
 * Reusable setting item component
 */
@Composable
private fun SettingItem(
	title: String,
	modifier: Modifier = Modifier,
	icon: ImageVector? = null,
	description: String? = null,
	value: String? = null,
	onClick: (() -> Unit)? = null,
	showChevron: Boolean = false
) {
	val clickModifier = if (onClick != null) {
		modifier.clickable(
			onClick = onClick,
			indication = ripple(bounded = true),
			interactionSource = remember { MutableInteractionSource() }
		)
	} else {
		modifier
	}

	Row(
		modifier = clickModifier
			.fillMaxWidth()
			.padding(horizontal = Spacing.medium, vertical = Spacing.small),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		// Left: Icon + Text
		Row(
			modifier = Modifier.weight(1f),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
		) {
			// Optional Icon
			if (icon != null) {
				Icon(
					imageVector = icon,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.size(Dimensions.iconMedium)
				)
			}

			// Title and Description
			Column {
				Text(
					text = title,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface
				)
				if (description != null) {
					Text(
						text = description,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}

		// Right: Value + Chevron
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(Spacing.small)
		) {
			if (value != null) {
				Text(
					text = value,
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			if (showChevron) {
				Icon(
					imageVector = Icons.AutoMirrored.Filled.ArrowForward,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
					modifier = Modifier.size(Dimensions.iconMedium)
				)
			}
		}
	}
}

/**
 * Language picker dialog
 */
@Composable
private fun LanguagePickerDialog(
	selectedLanguage: Language,
	onLanguageSelected: (Language) -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = {
			Text(
				text = stringResource(R.string.dialog_language_title),
				style = MaterialTheme.typography.titleLarge
			)
		},
		text = {
			Column {
				Language.values().forEach { language ->
					val languageText = stringResource(
						when (language) {
							Language.ENGLISH -> R.string.dialog_language_english
							Language.INDONESIAN -> R.string.dialog_language_indonesian
						}
					)
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable {
								onLanguageSelected(language)
							}
							.padding(vertical = Spacing.small),
						verticalAlignment = Alignment.CenterVertically
					) {
						RadioButton(
							selected = selectedLanguage == language,
							onClick = {
								onLanguageSelected(language)
							}
						)
						Spacer(modifier = Modifier.width(Spacing.small))
						Text(
							text = languageText,
							style = MaterialTheme.typography.bodyLarge
						)
					}
				}
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.dialog_cancel))
			}
		}
	)
}

/**
 * Clear cache confirmation dialog
 */
@Composable
private fun ClearCacheDialog(
	onConfirm: () -> Unit,
	onDismiss: () -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		icon = {
			Icon(
				imageVector = Icons.Default.Delete,
				contentDescription = null,
				modifier = Modifier.size(Dimensions.iconLarge)
			)
		},
		title = {
			Text(text = stringResource(R.string.dialog_clear_cache_title))
		},
		text = {
			Text(
				text = stringResource(R.string.dialog_clear_cache_message),
				style = MaterialTheme.typography.bodyMedium
			)
		},
		confirmButton = {
			Button(onClick = onConfirm) {
				Text(stringResource(R.string.dialog_clear_cache_confirm))
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text(stringResource(R.string.dialog_cancel))
			}
		}
	)
}

/**
 * Theme mode enum
 */
enum class ThemeMode(val icon: ImageVector) {
	SYSTEM(Icons.Default.Phone),
	LIGHT(Icons.Default.Settings),
	DARK(Icons.Default.Info)
}

/**
 * Language enum - Only English and Indonesian
 */
enum class Language(val displayName: String, val code: String) {
	ENGLISH("English", "en"),
	INDONESIAN("Bahasa Indonesia", "in")
}

// Preview composables
@Preview(name = "Settings Screen - Light", showBackground = true)
@Composable
private fun SettingsScreenPreviewLight() {
	ComposemediaTheme(darkTheme = false) {
		SettingsScreen()
	}
}

@Preview(name = "Settings Screen - Dark", showBackground = true)
@Composable
private fun SettingsScreenPreviewDark() {
	ComposemediaTheme(darkTheme = true) {
		SettingsScreen()
	}
}

@Preview(name = "Language Dialog", showBackground = true)
@Composable
private fun LanguageDialogPreview() {
	ComposemediaTheme {
		LanguagePickerDialog(
			selectedLanguage = Language.ENGLISH,
			onLanguageSelected = {},
			onDismiss = {}
		)
	}
}

@Preview(name = "Clear Cache Dialog", showBackground = true)
@Composable
private fun ClearCacheDialogPreview() {
	ComposemediaTheme {
		ClearCacheDialog(
			onConfirm = {},
			onDismiss = {}
		)
	}
}
