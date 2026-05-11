package com.iptvplayer.data.repository

import com.iptvplayer.core.DispatcherProvider
import com.iptvplayer.core.Result
import com.iptvplayer.core.runCatching
import com.iptvplayer.data.local.database.EpgProgrammeDao
import com.iptvplayer.data.local.entities.toDomain
import com.iptvplayer.data.local.entities.toEntity
import com.iptvplayer.data.parser.XmlTvParser
import com.iptvplayer.data.remote.KtorClient
import com.iptvplayer.domain.model.EpgProgramme
import com.iptvplayer.domain.repository.EpgRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

class EpgRepositoryImpl(
    private val programmeDao: EpgProgrammeDao,
    private val ktorClient: KtorClient,
    private val xmlTvParser: XmlTvParser,
    private val dispatcherProvider: DispatcherProvider
) : EpgRepository {

    override suspend fun fetchAndCacheEpg(playlistIds: List<String>): Result<Unit> = runCatching {
        withContext(dispatcherProvider.io) {
            // TODO: Fetch XMLTV, parse, batch insert
            Unit
        }
    }

    override fun getEpgForChannel(
        channelId: String,
        from: Instant,
        to: Instant
    ): Flow<List<EpgProgramme>> =
        programmeDao.getByChannelAndTimeRange(
            channelId = channelId,
            fromEpoch = from.toEpochMilli(),
            toEpoch = to.toEpochMilli()
        ).map { entities -> entities.map { it.toDomain() } }

    override fun getNowPlaying(channelId: String): Flow<EpgProgramme?> {
        // TODO: Query for programme where now is between startAt and endAt
        return kotlinx.coroutines.flow.flow { emit(null) }
    }

    override suspend fun clearStaleEpg(before: Instant): Result<Unit> = runCatching {
        withContext(dispatcherProvider.io) {
            programmeDao.deleteStale(before.toEpochMilli())
        }
    }
}
