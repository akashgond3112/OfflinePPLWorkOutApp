package com.example.offlinepplworkoutapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkTeal,
    onPrimary = TextOnPrimary,
    primaryContainer = DarkOrange,
    onPrimaryContainer = TextOnPrimary,
    secondary = DarkOrange,
    onSecondary = TextOnPrimary,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkTeal,
    tertiary = TealSecondary,
    onTertiary = TextOnPrimary,
    tertiaryContainer = DarkSurface,
    onTertiaryContainer = DarkTeal,
    error = PrimaryCoral,
    onError = TextOnPrimary,
    errorContainer = DarkOrange,
    onErrorContainer = TextOnPrimary,
    background = DarkBackground,
    onBackground = TextOnPrimary,
    surface = DarkSurface,
    onSurface = TextOnPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkTeal,
    outline = DarkTeal,
    outlineVariant = DarkOrange,
    scrim = Black,
    inverseSurface = CardBackground,
    inverseOnSurface = TextPrimary,
    inversePrimary = PrimaryCoral
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCoral,
    onPrimary = TextOnPrimary,
    primaryContainer = AmberAccent,
    onPrimaryContainer = TextPrimary,
    secondary = TealSecondary,
    onSecondary = TextOnPrimary,
    secondaryContainer = CardBackgroundSubtle,
    onSecondaryContainer = TextPrimary,
    tertiary = BluePrimary,
    onTertiary = TextOnPrimary,
    tertiaryContainer = BackgroundGradient,
    onTertiaryContainer = TextPrimary,
    error = PrimaryCoral,
    onError = TextOnPrimary,
    errorContainer = CardBackgroundSubtle,
    onErrorContainer = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardBackgroundSubtle,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    outlineVariant = DividerColor,
    scrim = ShadowColor,
    inverseSurface = DarkSurface,
    inverseOnSurface = TextOnPrimary,
    inversePrimary = TealSecondary
)

@Composable
fun OfflinePPLWorkOutAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to use our custom colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
