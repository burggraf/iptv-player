package com.iptvplayer

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.iptvplayer.data.worker.PlaylistRefreshWorker
import com.iptvplayer.di.appModule
import com.iptvplayer.di.dataModule
import com.iptvplayer.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import java.util.concurrent.TimeUnit

class IptvPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@IptvPlayerApp)
            modules(appModule, dataModule, domainModule)
        }
        schedulePlaylistRefresh()
    }

    private fun schedulePlaylistRefresh() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<PlaylistRefreshWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "playlist_refresh",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
