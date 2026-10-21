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
  primary = GoldLight,
  onPrimary = NavyDark,
  primaryContainer = NavyDark,
  onPrimaryContainer = GoldLight,
  secondary = SlateLight,
  onSecondary = Color.Black,
  tertiary = GoldAccent,
  background = BackgroundDark,
  surface = SurfaceDark,
  surfaceVariant = SurfaceContainerDark,
  onBackground = OnSurfaceDark,
  onSurface = OnSurfaceDark,
  onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
  primary = NavyPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE8EAF6),
  onPrimaryContainer = NavyDark,
  secondary = SlateSecondary,
  onSecondary = Color.White,
  tertiary = GoldDark,
  onTertiary = Color.White,
  background = BackgroundLight,
  surface = SurfaceLight,
  surfaceVariant = SurfaceContainerLight,
  onBackground = OnSurfaceLight,
  onSurface = OnSurfaceLight,
  onSurfaceVariant = OnSurfaceVariantLight,
  error = CoralError
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent academy brand colors
  content: @Composable () -> Unit,
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
