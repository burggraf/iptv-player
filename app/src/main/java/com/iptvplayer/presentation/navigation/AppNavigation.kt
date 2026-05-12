package com.iptvplayer.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iptvplayer.presentation.screens.addplaylist.AddPlaylistScreen
import com.iptvplayer.presentation.screens.epg.EpgScreen
import com.iptvplayer.presentation.screens.favorites.FavoritesScreen
import com.iptvplayer.presentation.screens.fullscreen.FullscreenPlayerScreen
import com.iptvplayer.presentation.screens.home.HomeScreen
import com.iptvplayer.presentation.screens.settings.SettingsScreen
import com.iptvplayer.presentation.viewmodel.EpgViewModel
import com.iptvplayer.presentation.viewmodel.FavoritesViewModel
import com.iptvplayer.presentation.viewmodel.PlayerViewModel
import com.iptvplayer.presentation.viewmodel.PlaylistViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Epg : Screen("epg")
    object FullscreenPlayer : Screen("fullscreen")
    object Settings : Screen("settings")
    object AddPlaylist : Screen("add_playlist")
    object Favorites : Screen("favorites")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = koinViewModel()
    val epgViewModel: EpgViewModel = koinViewModel()
    val playlistViewModel: PlaylistViewModel = koinViewModel()
    val favoritesViewModel: FavoritesViewModel = koinViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val playlistState by playlistViewModel.uiState.collectAsState()
            HomeScreen(
                playlists = playlistState.playlists,
                selectedPlaylistId = playlistState.selectedPlaylistId,
                onNavigateToEpg = { navController.navigate(Screen.Epg.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAddPlaylist = { navController.navigate(Screen.AddPlaylist.route) },
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onPlaylistSelected = { playlistViewModel.selectPlaylist(it) },
                onPlaylistRemoved = { playlistViewModel.removePlaylist(it) },
                onPlaylistRefreshed = { playlistViewModel.refreshPlaylist(it) },
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
        composable(Screen.AddPlaylist.route) {
            AddPlaylistScreen(
                onAddM3uUrl = { name, url ->
                    playlistViewModel.addPlaylist(
                        name = name,
                        type = com.iptvplayer.domain.model.PlaylistType.M3U_URL,
                        url = url,
                    )
                    navController.popBackStack()
                },
                onAddXtream = { name, server, user, pass ->
                    playlistViewModel.addPlaylist(
                        name = name,
                        type = com.iptvplayer.domain.model.PlaylistType.XTREAM,
                        serverUrl = server,
                        username = user,
                        password = pass,
                    )
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.Favorites.route) {
            val favState by favoritesViewModel.uiState.collectAsState()
            FavoritesScreen(
                favorites = favState.favorites,
                onChannelClick = { channelId ->
                    // TODO: resolve channel by ID and play
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
