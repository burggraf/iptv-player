package com.iptvplayer.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.iptvplayer.domain.repository.EpgRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager worker for periodic EPG refresh.
 * Runs silently in the background every N hours (default 6h).
 * Only fetches programmes not yet cached.
 */
class EpgRefreshWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    private val epgRepository: EpgRepository by inject()

    override suspend fun doWork(): Result {
        if (runAttemptCount > MAX_RETRIES) {
            return Result.failure()
        }

        // Fetch EPG for all playlists (empty list = all)
        epgRepository.fetchAndCacheEpg(emptyList())

        // Clean up stale programmes older than 24h
        val cutoff = java.time.Instant.now().minusSeconds(24 * 3600)
        epgRepository.clearStaleEpg(cutoff)

        return Result.success()
    }

    companion object {
        private const val MAX_RETRIES = 3
        const val WORK_NAME = "epg_refresh"
    }
}
