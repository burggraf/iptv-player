package com.iptvplayer.di

import com.iptvplayer.data.local.database.AppDatabase
import com.iptvplayer.data.parser.M3uParser
import com.iptvplayer.data.parser.XmlTvParser
import com.iptvplayer.data.remote.KtorClient
import com.iptvplayer.data.repository.EpgRepositoryImpl
import com.iptvplayer.data.repository.PlaylistRepositoryImpl
import com.iptvplayer.data.repository.PlaybackRepositoryImpl
import com.iptvplayer.domain.repository.EpgRepository
import com.iptvplayer.domain.repository.PlaybackRepository
import com.iptvplayer.domain.repository.PlaylistRepository
import org.koin.dsl.module

val dataModule = module {
    single { KtorClient.create() }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistDao = get(),
            channelDao = get(),
            ktorClient = get(),
            m3uParser = get(),
            dispatcherProvider = get()
        )
    }

    single<EpgRepository> {
        EpgRepositoryImpl(
            programmeDao = get(),
            ktorClient = get(),
            xmlTvParser = get(),
            dispatcherProvider = get()
        )
    }

    single<PlaybackRepository> {
        PlaybackRepositoryImpl(
            context = org.koin.android.ext.koin.androidContext().get()
        )
    }

    single { M3uParser() }
    single { XmlTvParser() }
}
