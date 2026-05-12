package com.iptvplayer.domain.usecase

import com.iptvplayer.core.AppResult
import com.iptvplayer.core.runCatchingSuspend
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.repository.PlaybackRepository

class PlayChannelUseCase(
    private val repository: PlaybackRepository
) {
    suspend operator fun invoke(channel: Channel): AppResult<Unit> = runCatchingSuspend {
        repository.playChannel(channel)
    }
}
