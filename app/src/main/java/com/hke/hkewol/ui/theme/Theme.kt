package com.hke.hkewol.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // 自定义黄绿色主题
//    primary = Color(0xFF8BC34A),        // 浅黄绿色
//    secondary = Color(0xFFCDDC39),      // 酸橙色
//    tertiary = Color(0xFF4CAF50)        // 绿色
)

private val DarkColorScheme = darkColorScheme(
    // 自定义黄绿色深色主题
//    primary = Color(0xFF689F38),        // 深黄绿色
//    secondary = Color(0xFFAFB42B),      // 深酸橙色
//    tertiary = Color(0xFF388E3C)        // 深绿色
)

@Composable
fun HkeWolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 动态颜色在 Android 12+ 上可用，默认关闭以使用自定义黄绿色主题
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}