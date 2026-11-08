package app.ocphotos.ui.theme

import androidx.compose.ui.graphics.Color

// ocphotos brand (sky). Light and dark Material 3 color roles.

val BrandBlue = Color(0xFF0EA5E9)
val BrandBlueDark = Color(0xFF7FD1FA)

val LightColors = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFF0EA5E9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCDE5FF),
    onPrimaryContainer = Color(0xFF001D33),
    secondary = Color(0xFF4D616C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD0E5F3),
    onSecondaryContainer = Color(0xFF091E29),
    tertiary = Color(0xFF00668A),
    background = Color(0xFFF7F9FC),
    onBackground = Color(0xFF181C1F),
    surface = Color(0xFFF7F9FC),
    onSurface = Color(0xFF181C1F),
    surfaceVariant = Color(0xFFDCE3E9),
    onSurfaceVariant = Color(0xFF40484D),
    error = Color(0xFFBA1A1A),
    outline = Color(0xFF70787D),
)

val DarkColors = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF7FD1FA),
    onPrimary = Color(0xFF003548),
    primaryContainer = Color(0xFF004D6B),
    onPrimaryContainer = Color(0xFFC7E7FF),
    secondary = Color(0xFFB4CAD6),
    onSecondary = Color(0xFF1F333E),
    secondaryContainer = Color(0xFF354955),
    onSecondaryContainer = Color(0xFFD0E5F3),
    tertiary = Color(0xFF82D3F0),
    background = Color(0xFF0F1417),
    onBackground = Color(0xFFDFE3E7),
    surface = Color(0xFF0F1417),
    onSurface = Color(0xFFDFE3E7),
    surfaceVariant = Color(0xFF40484D),
    onSurfaceVariant = Color(0xFFBFC8CE),
    error = Color(0xFFFFB4AB),
    outline = Color(0xFF89939A),
)
