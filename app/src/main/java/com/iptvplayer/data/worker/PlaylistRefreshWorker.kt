package com.iptvplayer.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.iptvplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration

/**
 * WorkManager worker for auto-refreshing playlists on boot / network change.
 * Runs periodically and re-fetches remote playlists (M3U URL, Xtream).
 * Skips local M3U_FILE playlists.
 */
class PlaylistRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val playlistRepository: PlaylistRepository by inject()

    override suspend fun doWork(): Result {
        if (runAttemptCount > MAX_RETRIES) {
            return Result.failure()
        }

        val playlists = playlistRepository.getPlaylists().first()

        playlists.forEach { playlist ->
            if (shouldRefresh(playlist)) {
                playlistRepository.refreshPlaylist(playlist.id)
            }
        }

        return Result.success()
    }

    private fun shouldRefresh(playlist: com.iptvplayer.domain.model.Playlist): Boolean {
        // Skip local files — nothing to fetch
        if (playlist.type == com.iptvplayer.domain.model.PlaylistType.M3U_FILE) return false

        val elapsed = java.time.Duration.between(playlist.lastUpdated, java.time.Instant.now())
        return elapsed >= REFRESH_INTERVAL
    }

    companion object {
        private const val MAX_RETRIES = 3
        val REFRESH_INTERVAL: Duration = Duration.ofHours(6)
    }
}
