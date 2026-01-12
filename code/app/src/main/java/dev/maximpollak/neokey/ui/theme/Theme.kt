package dev.maximpollak.neokey.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeoMint,
    onPrimary = Color.Black,

    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = NeoMint,
    onPrimary = Color.Black,

    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun NEOKeyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // IMPORTANT: false so your NeoMint is not replaced by Material You colors
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
