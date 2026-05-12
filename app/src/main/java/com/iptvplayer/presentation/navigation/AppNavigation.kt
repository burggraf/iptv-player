package com.iptvplayer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iptvplayer.presentation.screens.epg.EpgScreen
import com.iptvplayer.presentation.screens.fullscreen.FullscreenPlayerScreen
import com.iptvplayer.presentation.screens.home.HomeScreen
import com.iptvplayer.presentation.screens.settings.SettingsScreen
import com.iptvplayer.presentation.viewmodel.PlayerViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Epg : Screen("epg")
    object FullscreenPlayer : Screen("fullscreen")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = koinViewModel()

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
                playerViewModel = playerViewModel,
                onNavigateToFullscreen = {
                    navController.navigate(Screen.FullscreenPlayer.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.FullscreenPlayer.route) {
            val currentChannel by playerViewModel.currentChannel.collectAsState()
            val playbackState by playerViewModel.playbackState.collectAsState()

            FullscreenPlayerScreen(
                player = playerViewModel.player,
                channel = currentChannel,
                playbackState = playbackState,
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
