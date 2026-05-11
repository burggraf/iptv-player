package com.iptvplayer.domain.usecase

import com.iptvplayer.core.Result
import com.iptvplayer.core.runCatching
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.repository.PlaybackRepository

class PlayChannelUseCase(
    private val repository: PlaybackRepository
) {
    suspend operator fun invoke(channel: Channel): Result<Unit> = runCatching {
        repository.prepareChannel(channel)
    }
}
