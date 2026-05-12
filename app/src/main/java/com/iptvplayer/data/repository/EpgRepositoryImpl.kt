package com.iptvplayer.data.repository

import com.iptvplayer.core.AppResult
import com.iptvplayer.core.Constants
import com.iptvplayer.core.runCatchingSuspend
import com.iptvplayer.data.local.database.EpgProgrammeDao
import com.iptvplayer.data.local.entities.EpgProgrammeEntity
import com.iptvplayer.data.local.entities.toDomain as toDomainProgramme
import com.iptvplayer.data.local.entities.toEntity
import com.iptvplayer.data.parser.XmlTvParser
import com.iptvplayer.data.remote.EpgApi
import com.iptvplayer.core.DispatcherProvider
import com.iptvplayer.domain.model.EpgProgramme
import com.iptvplayer.domain.repository.EpgRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant

class EpgRepositoryImpl(
    private val programmeDao: EpgProgrammeDao,
    private val epgApi: EpgApi,
    private val xmlTvParser: XmlTvParser,
    private val dispatcherProvider: DispatcherProvider
) : EpgRepository {

    override suspend fun fetchAndCacheEpg(playlistIds: List<String>): AppResult<Unit> = runCatchingSuspend {
        withContext(dispatcherProvider.io) {
            // TODO: Fetch XMLTV URL per playlist, parse, batch insert
            // 1. Get playlist URLs from repository
            // 2. For each URL: fetch XMLTV, parse
            // 3. Batch insert programmes in chunks of 500
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
        ).map { entities -> entities.map { it.toDomainProgramme() } }

    override fun getNowPlaying(channelId: String): Flow<EpgProgramme?> = flow {
        val now = Instant.now()
        val entities: List<EpgProgrammeEntity> = programmeDao
            .getByChannelAndTimeRange(channelId, now.toEpochMilli(), now.toEpochMilli())
            .first()
        val current = entities.firstOrNull { entity ->
            val p = entity.toDomainProgramme()
            now.isAfter(p.startAt) && now.isBefore(p.endAt)
        }
        emit(current?.toDomainProgramme())
    }

    override suspend fun clearStaleEpg(before: Instant): AppResult<Unit> = runCatchingSuspend {
        withContext(dispatcherProvider.io) {
            programmeDao.deleteStale(before.toEpochMilli())
        }
    }
}
