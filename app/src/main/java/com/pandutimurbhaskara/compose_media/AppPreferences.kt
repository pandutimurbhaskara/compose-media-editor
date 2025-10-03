package com.pandutimurbhaskara.compose_media

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Simple SharedPreferences manager for app settings
 */
class AppPreferences(context: Context) {
	private val prefs: SharedPreferences = context.getSharedPreferences(
		PREFS_NAME,
		Context.MODE_PRIVATE
	)

	companion object {
		private const val PREFS_NAME = "privacy_editor_prefs"
		private const val KEY_THEME_MODE = "theme_mode"
		private const val KEY_LANGUAGE = "language"

		// Theme modes
		const val THEME_SYSTEM = 0
		const val THEME_LIGHT = 1
		const val THEME_DARK = 2

		// Languages
		const val LANG_ENGLISH = "en"
		const val LANG_INDONESIAN = "in"
	}

	/**
	 * Get selected theme mode
	 */
	fun getThemeMode(): Int {
		return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
	}

	/**
	 * Save theme mode
	 */
	fun setThemeMode(mode: Int) {
		prefs.edit {
			putInt(KEY_THEME_MODE, mode)
		}
	}

	/**
	 * Get selected language
	 */
	fun getLanguage(): String {
		return prefs.getString(KEY_LANGUAGE, LANG_ENGLISH) ?: LANG_ENGLISH
	}

	/**
	 * Save language
	 */
	fun setLanguage(language: String) {
		prefs.edit {
			putString(KEY_LANGUAGE, language)
		}
	}
}
