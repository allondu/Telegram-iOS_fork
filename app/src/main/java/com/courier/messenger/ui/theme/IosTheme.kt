package com.courier.messenger.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Design tokens taken from the iOS client's presentation layer: system-blue accent,
 * grouped-table backgrounds, hairlines and the SF-style type ramp.
 */
@Immutable
data class IosColors(
    val accent: Color,
    val background: Color,
    val groupedBackground: Color,
    val navBar: Color,
    val navHairline: Color,
    val separator: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val searchField: Color,
    val inputField: Color,
    val chatBackground: Color,
    val incomingBubble: Color,
    val outgoingTop: Color,
    val outgoingBottom: Color,
    val outgoingText: Color,
    val badge: Color,
    val badgeMuted: Color,
    val online: Color,
    val danger: Color,
    val isDark: Boolean,
)

val IosLightColors = IosColors(
    accent = Color(0xFF007AFF),
    background = Color(0xFFFFFFFF),
    groupedBackground = Color(0xFFEFEFF4),
    navBar = Color(0xFFF9F9F9),
    navHairline = Color(0x66C8C7CC),
    separator = Color(0xFFD9D9DE),
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF8E8E93),
    textTertiary = Color(0xFFC7C7CC),
    searchField = Color(0xFFE9E9EB),
    inputField = Color(0xFFE9E9EB),
    chatBackground = Color(0xFFEFEFF4),
    incomingBubble = Color(0xFFFFFFFF),
    outgoingTop = Color(0xFF4FA7F7),
    outgoingBottom = Color(0xFF3390EC),
    outgoingText = Color(0xFFFFFFFF),
    badge = Color(0xFF007AFF),
    badgeMuted = Color(0xFF98989E),
    online = Color(0xFF34C759),
    danger = Color(0xFFFF3B30),
    isDark = false,
)

val IosDarkColors = IosColors(
    accent = Color(0xFF0A84FF),
    background = Color(0xFF000000),
    groupedBackground = Color(0xFF1C1C1E),
    navBar = Color(0xFF1C1C1E),
    navHairline = Color(0x66FFFFFF),
    separator = Color(0xFF38383A),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF98989F),
    textTertiary = Color(0xFF5A5A5F),
    searchField = Color(0xFF1C1C1E),
    inputField = Color(0xFF1C1C1E),
    chatBackground = Color(0xFF000000),
    incomingBubble = Color(0xFF262628),
    outgoingTop = Color(0xFF33689A),
    outgoingBottom = Color(0xFF2C5F8F),
    outgoingText = Color(0xFFFFFFFF),
    badge = Color(0xFF0A84FF),
    badgeMuted = Color(0xFF636366),
    online = Color(0xFF32D74B),
    danger = Color(0xFFFF453A),
    isDark = true,
)

val LocalIos = staticCompositionLocalOf { IosLightColors }

/** SF-like type ramp. */
object IosType {
    val largeTitle = TextStyle(fontSize = 34.sp, lineHeight = 41.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.37.sp)
    val title = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp)
    val headline = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp)
    val body = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.4).sp)
    val callout = TextStyle(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.3).sp)
    val subhead = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.24).sp)
    val footnote = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.08).sp)
    val caption = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal)
    val caption2 = TextStyle(fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Normal)
    val tabLabel = TextStyle(fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.1.sp)
    val sectionHeader = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.08).sp)
}

@Composable
fun IosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) IosDarkColors else IosLightColors
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent,
            background = colors.background,
            surface = colors.background,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            background = colors.background,
            surface = colors.background,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
        )
    }
    CompositionLocalProvider(LocalIos provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
