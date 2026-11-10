package com.familyguard.calls.ui

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

/** Material 3 with dynamic colours taken from the wallpaper (Android 12+), light and dark. */
@Composable
fun GuardTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val scheme = when {
        Build.VERSION.SDK_INT >= 31 -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = scheme, content = content)
}

/** Fixed status colours (green = protected, amber = something missing). */
object StatusColors {
    @Composable fun okContainer() = if (isSystemInDarkTheme()) Color(0xFF1E4D2B) else Color(0xFFD5F5DC)
    @Composable fun onOk() = if (isSystemInDarkTheme()) Color(0xFFB7F0C4) else Color(0xFF0B3D1A)
    @Composable fun warnContainer() = if (isSystemInDarkTheme()) Color(0xFF5C3B00) else Color(0xFFFFE3B8)
    @Composable fun onWarn() = if (isSystemInDarkTheme()) Color(0xFFFFDDB0) else Color(0xFF4A2C00)
}
