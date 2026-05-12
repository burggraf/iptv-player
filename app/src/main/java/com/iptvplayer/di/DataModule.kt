package com.iptvplayer.di

import com.iptvplayer.data.local.database.AppDatabase
import com.iptvplayer.data.parser.M3uParser
import com.iptvplayer.data.parser.XmlTvParser
import com.iptvplayer.data.parser.XtreamParser
import com.iptvplayer.data.remote.EpgApi
import com.iptvplayer.data.remote.PlaylistApi
import com.iptvplayer.data.repository.EpgRepositoryImpl
import com.iptvplayer.data.repository.FavoritesRepositoryImpl
import com.iptvplayer.data.repository.PlaylistRepositoryImpl
import com.iptvplayer.data.repository.PlaybackRepositoryImpl
import com.iptvplayer.domain.repository.EpgRepository
import com.iptvplayer.domain.repository.FavoritesRepository
import com.iptvplayer.domain.repository.PlaybackRepository
import com.iptvplayer.domain.repository.PlaylistRepository
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single<HttpClient> { com.iptvplayer.data.remote.KtorClient.create() }
    single { PlaylistApi(get()) }
    single { EpgApi(get()) }

    single<PlaylistRepository> {
        PlaylistRepositoryImpl(
            playlistDao = get(),
            channelDao = get(),
            playlistApi = get(),
            m3uParser = get(),
            xtreamParser = get(),
            dispatcherProvider = get()
        )
    }

    single<EpgRepository> {
        EpgRepositoryImpl(
            programmeDao = get(),
            epgApi = get(),
            xmlTvParser = get(),
            dispatcherProvider = get()
        )
    }

    single<PlaybackRepository> {
        PlaybackRepositoryImpl(
            context = androidContext()
        )
    }

    single { M3uParser() }
    single { XmlTvParser() }
    single { XtreamParser() }

    single<FavoritesRepository> {
        FavoritesRepositoryImpl(
            favoriteDao = get(),
            dispatcherProvider = get()
        )
    }
}
