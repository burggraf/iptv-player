package com.iptvplayer.di

import com.iptvplayer.domain.usecase.FetchEpgUseCase
import com.iptvplayer.domain.usecase.LoadPlaylistUseCase
import com.iptvplayer.domain.usecase.PlayChannelUseCase
import com.iptvplayer.presentation.viewmodel.EpgViewModel
import com.iptvplayer.presentation.viewmodel.FavoritesViewModel
import com.iptvplayer.presentation.viewmodel.PlayerViewModel
import com.iptvplayer.presentation.viewmodel.PlaylistViewModel
import com.iptvplayer.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val domainModule = module {
    factory { LoadPlaylistUseCase(get()) }
    factory { FetchEpgUseCase(get()) }
    factory { PlayChannelUseCase(get()) }

    viewModel {
        PlayerViewModel(
            playbackRepository = get(),
            playChannelUseCase = get()
        )
    }

    viewModel {
        EpgViewModel(
            fetchEpgUseCase = get()
        )
    }

    viewModel {
        PlaylistViewModel(
            playlistRepository = get()
        )
    }

    viewModel {
        FavoritesViewModel(
            favoritesRepository = get()
        )
    }

    viewModel {
        SettingsViewModel(
            settingsRepository = get()
        )
    }
}
