package com.iptvplayer.domain.usecase

import com.iptvplayer.core.Result
import com.iptvplayer.core.runCatching
import com.iptvplayer.domain.repository.EpgRepository

class FetchEpgUseCase(
    private val repository: EpgRepository
) {
    suspend operator fun invoke(playlistIds: List<String>): Result<Unit> = runCatching {
        repository.fetchAndCacheEpg(playlistIds)
    }
}
