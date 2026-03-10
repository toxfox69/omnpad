package com.energenai.omnpad.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Void = Color(0xFF0A0A0F)
val Surface = Color(0xFF12121A)
val SurfaceVariant = Color(0xFF1A1A24)
val CardBg = Color(0xFF16161F)
val Border = Color(0xFF2A2A3A)
val TextPrimary = Color(0xFFE0E0E8)
val TextSecondary = Color(0xFF8888AA)
val Accent = Color(0xFF00FFD1)
val AccentDim = Color(0xFF00B894)
val Magenta = Color(0xFFFF2D7B)
val Cyan = Color(0xFF00D4FF)
val Amber = Color(0xFFFFB300)
val ErrorRed = Color(0xFFFF4444)

// Syntax highlighting colors
val SynKeyword = Color(0xFFFF79C6)
val SynString = Color(0xFFF1FA8C)
val SynComment = Color(0xFF6272A4)
val SynNumber = Color(0xFFBD93F9)
val SynFunction = Color(0xFF50FA7B)
val SynType = Color(0xFF8BE9FD)
val SynOperator = Color(0xFFFF6E6E)

private val DarkColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = Void,
    secondary = Magenta,
    tertiary = Cyan,
    background = Void,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    outline = Border,
)

@Composable
fun OmniPadTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
