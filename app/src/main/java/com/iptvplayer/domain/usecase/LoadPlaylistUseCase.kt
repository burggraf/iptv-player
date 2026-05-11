package com.iptvplayer.domain.usecase

import com.iptvplayer.core.Result
import com.iptvplayer.core.runCatching
import com.iptvplayer.domain.model.Playlist
import com.iptvplayer.domain.repository.PlaylistRepository

class LoadPlaylistUseCase(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlist: Playlist): Result<Unit> = runCatching {
        repository.addPlaylist(playlist)
    }
}
