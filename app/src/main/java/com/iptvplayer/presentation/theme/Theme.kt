package com.iptvplayer.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography

// ─── Color Palette ─────────────────────────────────────────────
// Deep dark backgrounds for TV — never pure black, always rich tones
object AppColors {
    // Backgrounds
    val Background = Color(0xFF0A0E14)
    val Surface = Color(0xFF141820)
    val SurfaceElevated = Color(0xFF1A2030)
    val SurfaceCard = Color(0xFF1C2434)
    val SurfaceFocus = Color(0xFF253048)

    // Accent — vibrant blue for focus/primary actions
    val Primary = Color(0xFF4C9AFF)
    val PrimaryDim = Color(0xFF3578D4)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFF1A2D4A)

    // Secondary — teal for secondary actions
    val Secondary = Color(0xFF36D399)
    val SecondaryContainer = Color(0xFF1A3428)

    // Status colors
    val Success = Color(0xFF36D399)
    val Warning = Color(0xFFFFB020)
    val Error = Color(0xFFFF5C5C)
    val OnError = Color.White

    // Text
    val TextPrimary = Color(0xFFF0F2F5)
    val TextSecondary = Color(0xFFB0B8C4)
    val TextTertiary = Color(0xFF6B7A8D)
    val TextInverse = Color(0xFF0A0E14)

    // Focus highlight — bright ring for selected items
    val FocusBorder = Color(0xFF4C9AFF)
    val FocusGlow = Color(0x404C9AFF)

    // Gradients
    val HeaderGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF0D1520), Color(0xFF141E2E), Color(0xFF0D1520))
    )
    val CardGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1C2434), Color(0xFF141820))
    )

    // EPG-specific
    val EpgBackground = Color(0xFF0E1420)
    val EpgHeader = Color(0xFF141E2E)
    val EpgChannelNameBg = Color(0xFF162032)
    val EpgProgrammeBg = Color(0xFF121A28)
    val EpgProgrammeNow = Color(0xFF1A2D4A)
    val EpgNowPlayingBadge = Color(0xFFE94560)
    val EpgTimeIndicator = Color(0xFFFF5C5C)
    val EpgGroupSelected = Color(0xFF1A3050)
    val EpgGroupUnselected = Color(0xFF141E2E)

    // Shimmer
    val ShimmerBase = Color(0xFF1A2030)
    val ShimmerHighlight = Color(0xFF253048)
}

// ─── Composition Locals ────────────────────────────────────────
val LocalFocusColor = staticCompositionLocalOf { AppColors.FocusBorder }

// ─── Color Scheme ─────────────────────────────────────────────
val DarkTvColorScheme: ColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.PrimaryContainer,
    onPrimaryContainer = AppColors.TextPrimary,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnPrimary,
    secondaryContainer = AppColors.SecondaryContainer,
    onSecondaryContainer = AppColors.TextPrimary,
    background = AppColors.Background,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.SurfaceElevated,
    onSurfaceVariant = AppColors.TextSecondary,
    error = AppColors.Error,
    onError = AppColors.OnError,
    inverseSurface = AppColors.TextPrimary,
    inverseOnSurface = AppColors.TextInverse,
    inversePrimary = AppColors.PrimaryDim,
)

// ─── Theme ────────────────────────────────────────────────────
object IptvPlayerTheme {
    val typography = Typography()

    @Composable
    fun Content(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = DarkTvColorScheme,
            typography = typography,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides AppColors.TextPrimary,
                    LocalFocusColor provides AppColors.FocusBorder,
                ) {
                    content()
                }
            }
        }
    }
}
