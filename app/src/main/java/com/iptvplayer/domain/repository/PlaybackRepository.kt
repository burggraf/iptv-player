package com.iptvplayer.domain.repository

import androidx.media3.common.Player
import com.iptvplayer.core.Result
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState
import kotlinx.coroutines.flow.Flow

interface PlaybackRepository {
    fun createPlayer(): Player
    suspend fun prepareChannel(channel: Channel): Result<Unit>
    fun getPlaybackState(): Flow<PlaybackState>
}
