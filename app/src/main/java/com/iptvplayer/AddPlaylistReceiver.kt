package com.iptvplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.model.PlaylistType
import com.iptvplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin
import java.time.Instant
import java.util.UUID

/**
 * Broadcast receiver for adding playlists via adb shell am broadcast.
 * Usage:
 *   adb shell am broadcast -a com.iptvplayer.ACTION_ADD_PLAYLIST \
 *     --es name "My Playlist" \
 *     --es type "XTREAM" \
 *     --es serverUrl "http://example.com:8080" \
 *     --es username "user" \
 *     --es password "pass"
 */
class AddPlaylistReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ADD_PLAYLIST) return

        val name = intent.getStringExtra(EXTRA_NAME) ?: return
        val typeStr = intent.getStringExtra(EXTRA_TYPE) ?: return
        val type = PlaylistType.entries.find { it.name == typeStr } ?: return

        val url = intent.getStringExtra(EXTRA_URL)
        val serverUrl = intent.getStringExtra(EXTRA_SERVER_URL)
        val username = intent.getStringExtra(EXTRA_USERNAME)
        val password = intent.getStringExtra(EXTRA_PASSWORD)

        Log.d(TAG, "Adding playlist via broadcast: $name ($type)")

        scope.launch {
            try {
                val repository: PlaylistRepository = getKoin().get()
                val playlist = Playlist(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    type = type,
                    url = url,
                    serverUrl = serverUrl,
                    username = username,
                    password = password,
                    addedAt = Instant.now(),
                    lastUpdated = Instant.now(),
                )
                repository.addPlaylist(playlist)
                Log.d(TAG, "Playlist added successfully: $name")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add playlist: $name", e)
            }
        }
    }

    companion object {
        const val TAG = "AddPlaylistReceiver"
        const val ACTION_ADD_PLAYLIST = "com.iptvplayer.ACTION_ADD_PLAYLIST"
        const val EXTRA_NAME = "name"
        const val EXTRA_TYPE = "type"
        const val EXTRA_URL = "url"
        const val EXTRA_SERVER_URL = "serverUrl"
        const val EXTRA_USERNAME = "username"
        const val EXTRA_PASSWORD = "password"
    }
}
