package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BluePrimary,
    secondary = SophisticatedBorderAccent,
    tertiary = AccentPillBg,
    background = SophisticatedBackground,
    surface = SophisticatedSurface,
    onBackground = CodeText,
    onSurface = CodeText
  )

private val LightColorScheme = DarkColorScheme // Preserving premium dark for all

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force premium dark mode for coder aesthetics
  dynamicColor: Boolean = false, // Disable dynamic colors to keep coder colors
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
