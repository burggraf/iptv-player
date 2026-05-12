package com.iptvplayer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.iptvplayer.presentation.navigation.AddPlaylistRouteArgs
import com.iptvplayer.presentation.navigation.AppNavigation
import com.iptvplayer.presentation.screens.addplaylist.AddPlaylistScreen
import com.iptvplayer.presentation.theme.IptvPlayerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check for intent extras to pre-fill playlist form
        val initialArgs = extractPlaylistArgs(intent)
        
        setContent {
            IptvPlayerTheme.Content {
                AppNavigation(initialPlaylistArgs = initialArgs)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val args = extractPlaylistArgs(intent)
        if (args != null) {
            AddPlaylistScreen.initialArgs = args
        }
    }

    private fun extractPlaylistArgs(intent: Intent): AddPlaylistRouteArgs? {
        val name = intent.getStringExtra(EXTRA_PLAYLIST_NAME)
        val type = intent.getStringExtra(EXTRA_PLAYLIST_TYPE)
        val serverUrl = intent.getStringExtra(EXTRA_PLAYLIST_SERVER_URL)
        val url = intent.getStringExtra(EXTRA_PLAYLIST_URL)
        val username = intent.getStringExtra(EXTRA_PLAYLIST_USERNAME)
        val password = intent.getStringExtra(EXTRA_PLAYLIST_PASSWORD)

        return if (serverUrl != null || url != null) {
            AddPlaylistRouteArgs(
                name = name,
                type = type ?: "XTREAM",
                serverUrl = serverUrl,
                url = url,
                username = username,
                password = password,
            )
        } else null
    }

    companion object {
        const val EXTRA_PLAYLIST_NAME = "playlist_name"
        const val EXTRA_PLAYLIST_TYPE = "playlist_type"
        const val EXTRA_PLAYLIST_SERVER_URL = "playlist_server_url"
        const val EXTRA_PLAYLIST_URL = "playlist_url"
        const val EXTRA_PLAYLIST_USERNAME = "playlist_username"
        const val EXTRA_PLAYLIST_PASSWORD = "playlist_password"
    }
}
