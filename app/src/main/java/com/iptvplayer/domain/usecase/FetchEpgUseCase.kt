package com.iptvplayer.domain.usecase

import com.iptvplayer.core.AppResult
import com.iptvplayer.core.runCatchingSuspend
import com.iptvplayer.domain.repository.EpgRepository

class FetchEpgUseCase(
    private val repository: EpgRepository
) {
    suspend operator fun invoke(playlistIds: List<String>): AppResult<Unit> = runCatchingSuspend {
        repository.fetchAndCacheEpg(playlistIds)
    }
}
