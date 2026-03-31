package com.example.localservice.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de colores ServiLocal
private val Purple = Color(0xFF534AB7)
private val PurpleLight = Color(0xFFEEEDFE)
private val Teal = Color(0xFF1D9E75)
private val TealLight = Color(0xFFE1F5EE)
private val Coral = Color(0xFFD85A30)

private val LightColorScheme = lightColorScheme(
    primary          = Purple,
    onPrimary        = Color.White,
    primaryContainer = PurpleLight,
    onPrimaryContainer = Color(0xFF26215C),
    secondary        = Teal,
    onSecondary      = Color.White,
    secondaryContainer = TealLight,
    onSecondaryContainer = Color(0xFF04342C),
    error            = Coral,
    background       = Color(0xFFFAFAFA),
    surface          = Color.White,
    onBackground     = Color(0xFF1C1B1F),
    onSurface        = Color(0xFF1C1B1F),
    outline          = Color(0xFFB4B2A9)
)

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFFAFA9EC),
    onPrimary        = Color(0xFF26215C),
    primaryContainer = Color(0xFF3C3489),
    onPrimaryContainer = PurpleLight,
    secondary        = Color(0xFF5DCAA5),
    onSecondary      = Color(0xFF04342C),
    secondaryContainer = Color(0xFF0F6E56),
    onSecondaryContainer = TealLight,
    error            = Color(0xFFF0997B),
    background       = Color(0xFF1C1B1F),
    surface          = Color(0xFF2C2C2A),
    onBackground     = Color(0xFFE6E1E5),
    onSurface        = Color(0xFFE6E1E5)
)

@Composable
fun ServiLocalTheme(
    darkTheme: Boolean = false, // podés usar isSystemInDarkTheme() cuando quieras
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ServiLocalTypography,
        content = content
    )
}
