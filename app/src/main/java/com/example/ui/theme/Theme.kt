package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CoralFlame,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4D1F14),
    onPrimaryContainer = Color(0xFFFFDAD2),
    secondary = ElectricTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = ElectricTealSoft,
    background = NightBackground,
    onBackground = TextPrimaryDark,
    surface = NightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    outline = NightOutline,
    error = StatusExpiredDark,
    onError = Color.Black,
    errorContainer = StatusExpiredContainerDark,
    onErrorContainer = StatusExpiredContainer
)

private val LightColorScheme = lightColorScheme(
    primary = SlateNavy,
    onPrimary = Color.White,
    primaryContainer = CoralFlameSoft,
    onPrimaryContainer = Color(0xFF5C1506),
    secondary = CoralFlame,
    onSecondary = Color.White,
    secondaryContainer = ElectricTealSoft,
    onSecondaryContainer = Color(0xFF0F3D39),
    background = StudioBackground,
    onBackground = TextPrimaryLight,
    surface = StudioSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = StudioOutline,
    error = StatusExpired,
    onError = Color.White,
    errorContainer = StatusExpiredContainer,
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun GymAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
