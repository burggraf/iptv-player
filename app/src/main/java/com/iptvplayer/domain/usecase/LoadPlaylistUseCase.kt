package com.iptvplayer.domain.usecase

import com.iptvplayer.core.AppResult
import com.iptvplayer.core.runCatchingSuspend
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.repository.PlaylistRepository

class LoadPlaylistUseCase(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlist: Playlist): AppResult<Unit> = runCatchingSuspend {
        repository.addPlaylist(playlist)
    }
}
