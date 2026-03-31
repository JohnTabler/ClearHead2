package com.clearhead.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Terracotta / Rust
val Terracotta100 = Color(0xFFF9EBE4)
val Terracotta200 = Color(0xFFF0CDBA)
val Terracotta400 = Color(0xFFD4795A)
val Terracotta500 = Color(0xFFC4623E)
val Terracotta600 = Color(0xFFAD4E2C)
val Terracotta700 = Color(0xFF8B3A1E)

// Warm Sand / Cream
val Sand50  = Color(0xFFFDF6EE)
val Sand100 = Color(0xFFF5EADB)
val Sand200 = Color(0xFFE8D5BC)
val Sand300 = Color(0xFFD9BFA0)
val Sand400 = Color(0xFFC4A882)

// Sage Green
val Sage50  = Color(0xFFF1F4EF)
val Sage100 = Color(0xFFDDE6D8)
val Sage300 = Color(0xFF9DB896)
val Sage400 = Color(0xFF7A9E72)
val Sage500 = Color(0xFF5E8556)
val Sage600 = Color(0xFF426040)

// Warm Walnut / Brown
val Walnut100 = Color(0xFFEDE0D4)
val Walnut300 = Color(0xFFB8946E)
val Walnut500 = Color(0xFF7D5A3C)
val Walnut600 = Color(0xFF604229)
val Walnut800 = Color(0xFF3B2314)

val Parchment = Color(0xFFFBF7F2)
val WarmWhite = Color(0xFFF7F1EA)

val DarkBg      = Color(0xFF1E1712)
val DarkSurface = Color(0xFF26201A)
val DarkCard    = Color(0xFF312820)
val DarkBorder  = Color(0xFF453830)

val MigraineRed   = Color(0xFFBF4040)
val WarningAmber  = Color(0xFFD4872A)
val PositiveGreen = Color(0xFF5E8556)

private val LightColorScheme = lightColorScheme(
    primary            = Terracotta500,
    onPrimary          = Color.White,
    primaryContainer   = Terracotta100,
    onPrimaryContainer = Terracotta700,
    secondary          = Walnut500,
    onSecondary        = Color.White,
    secondaryContainer = Walnut100,
    onSecondaryContainer = Walnut600,
    tertiary           = Sage500,
    onTertiary         = Color.White,
    tertiaryContainer  = Sage100,
    onTertiaryContainer = Sage600,
    background         = Parchment,
    onBackground       = Walnut800,
    surface            = WarmWhite,
    onSurface          = Walnut800,
    surfaceVariant     = Sand100,
    onSurfaceVariant   = Walnut300,
    outline            = Sand200,
    error              = MigraineRed,
    onError            = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary            = Terracotta400,
    onPrimary          = Color(0xFF3D1A08),
    primaryContainer   = Terracotta600,
    onPrimaryContainer = Terracotta100,
    secondary          = Walnut300,
    onSecondary        = Color(0xFF2A1506),
    secondaryContainer = Walnut600,
    onSecondaryContainer = Walnut100,
    tertiary           = Sage300,
    onTertiary         = Color(0xFF142510),
    tertiaryContainer  = Sage600,
    onTertiaryContainer = Sage50,
    background         = DarkBg,
    onBackground       = Color(0xFFF2E8DC),
    surface            = DarkSurface,
    onSurface          = Color(0xFFF2E8DC),
    surfaceVariant     = DarkCard,
    onSurfaceVariant   = Color(0xFFCCB8A6),
    outline            = DarkBorder,
    error              = Color(0xFFFF8A8A),
    onError            = Color(0xFF4A0000)
)

val ClearHeadTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Light,    fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Light,    fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 26.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp),
)

@Composable
fun ClearHeadTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = ClearHeadTypography, content = content)
}
