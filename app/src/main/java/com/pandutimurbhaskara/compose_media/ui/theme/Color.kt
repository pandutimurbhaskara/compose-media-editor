package com.pandutimurbhaskara.compose_media.ui.theme

import androidx.compose.ui.graphics.Color

// Indigo Primary Colors - Material 3 Design
val Indigo500 = Color(0xFF3F51B5) // Primary color
val Indigo700 = Color(0xFF303F9F) // Dark variant
val Indigo200 = Color(0xFF9FA8DA) // Light variant
val IndigoAccent = Color(0xFF536DFE) // Accent color

// Light Theme Colors
val LightPrimary = Indigo500
val LightOnPrimary = Color.White
val LightPrimaryContainer = Indigo200
val LightOnPrimaryContainer = Color(0xFF001A41)

val LightSecondary = IndigoAccent
val LightOnSecondary = Color.White
val LightSecondaryContainer = Color(0xFFDEE3FF)
val LightOnSecondaryContainer = Color(0xFF001849)

val LightBackground = Color(0xFFFFFBFE)
val LightOnBackground = Color(0xFF1C1B1F)
val LightSurface = Color(0xFFFFFBFE)
val LightOnSurface = Color(0xFF1C1B1F)

// Dark Theme Colors
val DarkPrimary = Indigo200
val DarkOnPrimary = Color(0xFF00316D)
val DarkPrimaryContainer = Indigo700
val DarkOnPrimaryContainer = Color(0xFFD6E3FF)

val DarkSecondary = Color(0xFFBAC3FF)
val DarkOnSecondary = Color(0xFF00297A)
val DarkSecondaryContainer = Color(0xFF003FA2)
val DarkOnSecondaryContainer = Color(0xFFDEE3FF)

val DarkBackground = Color(0xFF1C1B1F)
val DarkOnBackground = Color(0xFFE4E2E6)
val DarkSurface = Color(0xFF1C1B1F)
val DarkOnSurface = Color(0xFFE4E2E6)

// Status Colors (for History screen badges)
val StatusDraft = Color(0xFFFFA726) // Orange/Amber
val StatusCompleted = Indigo500 // Indigo/Blue
val StatusExported = Color(0xFF26A69A) // Green/Teal
val StatusFailed = Color(0xFFE53935) // Red