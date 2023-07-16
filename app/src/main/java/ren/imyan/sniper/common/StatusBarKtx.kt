package ren.imyan.sniper.common

import android.graphics.Color
import android.view.View
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach

fun setTransparentStyle(
    view: View,
    window: Window,
    isLightTheme: Boolean = true
) {
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT
    WindowCompat.setDecorFitsSystemWindows(window, false)
    view.doOnAttach {
        setInsertContentTheme(window, view, isLightTheme)
        setInsertPadding(window, view)
    }
}

private fun setInsertContentTheme(
    window: Window,
    view: View,
    isLightTheme: Boolean
) {
    WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = isLightTheme
        isAppearanceLightNavigationBars = isLightTheme
    }
}

private fun setInsertPadding(window: Window, view: View) {
    val rootWindowInsert = ViewCompat.getRootWindowInsets(window.decorView) ?: return
    val statusInsert = rootWindowInsert.getInsets(WindowInsetsCompat.Type.statusBars())
    val paddingTop = kotlin.math.abs(statusInsert.top - statusInsert.bottom)
    val navInsert = rootWindowInsert.getInsets(WindowInsetsCompat.Type.navigationBars())
    val paddingBottom = kotlin.math.abs(navInsert.top - navInsert.bottom)
    if (paddingTop != 0 || paddingBottom != 0) {
        view.setPadding(0, paddingTop, 0, paddingBottom)
    }
}