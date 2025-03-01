package ren.imyan.sniper.ui.home

import androidx.compose.runtime.Composable

sealed class Screen(val route: String, val label: String, val content: @Composable () -> Unit) {
    object IconPackScreen : Screen("main_screen", "主页", content = {
        IconPackScreen()
    })

    object RequestScreen : Screen("request_screen", "请求", content = {
        RequestScreen()
    })
}
