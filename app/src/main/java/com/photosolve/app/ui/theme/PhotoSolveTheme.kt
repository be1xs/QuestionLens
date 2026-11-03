package com.photosolve.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Color(0xFF385E88), onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EFF8), onPrimaryContainer = Color(0xFF24466C),
    background = Color(0xFFF5F6F8), onBackground = Color(0xFF202731),
    surface = Color(0xFFFCFCFD), onSurface = Color(0xFF202731),
    surfaceVariant = Color(0xFFEBEEF2), onSurfaceVariant = Color(0xFF596575),
    outline = Color(0xFF748091), outlineVariant = Color(0xFFDCE1E8),
    secondaryContainer = Color(0xFFE8EFF8), onSecondaryContainer = Color(0xFF24466C),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFACC9ED), onPrimary = Color(0xFF173653),
    primaryContainer = Color(0xFF263C54), onPrimaryContainer = Color(0xFFD5E6FC),
    background = Color(0xFF12161C), onBackground = Color(0xFFE6EAF0),
    surface = Color(0xFF1C222B), onSurface = Color(0xFFE6EAF0),
    surfaceVariant = Color(0xFF2A323D), onSurfaceVariant = Color(0xFFB7C1CE),
    outline = Color(0xFF8B98A8), outlineVariant = Color(0xFF3D4856),
    secondaryContainer = Color(0xFF263C54), onSecondaryContainer = Color(0xFFD5E6FC),
)

@Composable
fun PhotoSolveTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp)), content = content)
}
