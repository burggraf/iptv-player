package com.iptvplayer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iptvplayer.presentation.screens.epg.EpgScreen
import com.iptvplayer.presentation.screens.fullscreen.FullscreenPlayerScreen
import com.iptvplayer.presentation.screens.home.HomeScreen
import com.iptvplayer.presentation.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Epg : Screen("epg")
    object FullscreenPlayer : Screen("fullscreen/{channelId}") {
        fun createRoute(channelId: String) = "fullscreen/$channelId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEpg = { navController.navigate(Screen.Epg.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Epg.route) {
            EpgScreen(
                onNavigateToFullscreen = { channelId ->
                    navController.navigate(Screen.FullscreenPlayer.createRoute(channelId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("fullscreen/{channelId}") {
            FullscreenPlayerScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
