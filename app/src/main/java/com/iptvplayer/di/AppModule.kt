package com.iptvplayer.di

import android.content.Context
import androidx.room.Room
import com.iptvplayer.data.local.database.AppDatabase
import com.iptvplayer.core.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { provideDatabase(androidContext()) }
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().channelDao() }
    single { get<AppDatabase>().epgChannelDao() }
    single { get<AppDatabase>().epgProgrammeDao() }
    single { get<AppDatabase>().favoriteDao() }
    single { DispatcherProvider(io = Dispatchers.IO, default = Dispatchers.Default, main = Dispatchers.Main) }
}

private fun provideDatabase(context: Context): AppDatabase =
    Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "iptvplayer.db"
    ).build()
