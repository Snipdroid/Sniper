package ren.imyan.sniper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ren.imyan.sniper.ui.about.About
import ren.imyan.sniper.ui.home.Home
import ren.imyan.sniper.ui.icon_detail.IconDetail

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("AppNavController Not Provide")
}

object Route {
    const val Home = "home"
    const val About = "about"
    const val IconDetail = "icon_detail/{category}"
}

@Composable
fun Nav() {
    val nav = rememberNavController()

    CompositionLocalProvider(LocalNavController provides nav) {
        NavHost(navController = nav, startDestination = "home") {
            composable("home") {
                Home()
            }
            composable("about") {
                About()
            }
            composable("icon_detail/{category}") {
                IconDetail(
                    it.arguments?.getString("category")
                )
            }
        }
    }
}