package com.iptvplayer.domain.repository

import com.iptvplayer.core.Result
import com.iptvplayer.domain.model.EpgProgramme
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface EpgRepository {
    suspend fun fetchAndCacheEpg(playlistIds: List<String>): Result<Unit>
    fun getEpgForChannel(channelId: String, from: Instant, to: Instant): Flow<List<EpgProgramme>>
    fun getNowPlaying(channelId: String): Flow<EpgProgramme?>
    suspend fun clearStaleEpg(before: Instant): Result<Unit>
}
