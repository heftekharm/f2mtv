package com.hfm.f2mtv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val ComposeDarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    onPrimary = Color.Black,
    secondary = SecondaryAccent,
    onSecondary = Color.White,
    tertiary = WarningGold,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = ComponentDark,
    surfaceContainerHigh = ButtonDark,
    outline = BorderDark,
    outlineVariant = TextMuted,
    error = ErrorRed,
    onError = Color.Black,
    scrim = Color.Black,
)

@OptIn(ExperimentalTvMaterial3Api::class)
private val TvDarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    onPrimary = Color.Black,
    secondary = SecondaryAccent,
    onSecondary = Color.White,
    tertiary = WarningGold,
    onTertiary = Color.Black,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    border = BorderDark,
    borderVariant = TextMuted,
    error = ErrorRed,
    onError = Color.Black,
    scrim = Color.Black,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun F2mtvTheme(
    @Suppress("UNUSED_PARAMETER") isInDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    androidx.compose.material3.MaterialTheme(
        colorScheme = ComposeDarkColorScheme,
        content = {
            MaterialTheme(
                colorScheme = TvDarkColorScheme,
                typography = Typography,
                content = content,
            )
        },
    )
}