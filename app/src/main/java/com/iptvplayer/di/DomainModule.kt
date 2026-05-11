package com.iptvplayer.di

import com.iptvplayer.domain.usecase.FetchEpgUseCase
import com.iptvplayer.domain.usecase.LoadPlaylistUseCase
import com.iptvplayer.domain.usecase.PlayChannelUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { LoadPlaylistUseCase(get()) }
    factory { FetchEpgUseCase(get()) }
    factory { PlayChannelUseCase(get()) }
}
