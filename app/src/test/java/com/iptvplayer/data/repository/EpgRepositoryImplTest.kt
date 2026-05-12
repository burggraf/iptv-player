package com.iptvplayer.data.repository

import com.iptvplayer.core.DispatcherProvider
import com.iptvplayer.data.local.database.EpgProgrammeDao
import com.iptvplayer.data.local.entities.EpgProgrammeEntity
import com.iptvplayer.data.parser.XmlTvParser
import com.iptvplayer.data.remote.EpgApi
import com.iptvplayer.core.AppResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

class EpgRepositoryImplTest {

    private val programmeDao: EpgProgrammeDao = mockk(relaxed = true)
    private val epgApi: EpgApi = mockk()
    private val xmlTvParser = XmlTvParser()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: EpgRepositoryImpl

    @Before
    fun setup() {
        repository = EpgRepositoryImpl(
            programmeDao = programmeDao,
            epgApi = epgApi,
            xmlTvParser = xmlTvParser,
            dispatcherProvider = DispatcherProvider(
                io = testDispatcher,
                default = testDispatcher,
                main = testDispatcher
            )
        )
    }

    @Test
    fun `get EPG for channel returns programmes in time range`() = runTest {
        val now = Instant.parse("2024-01-01T18:00:00Z")
        val entity = EpgProgrammeEntity(
            id = "p1", channelId = "BBC1.uk",
            title = "News", description = "Latest news", category = "News",
            startAt = Instant.parse("2024-01-01T18:00:00Z").toEpochMilli(),
            endAt = Instant.parse("2024-01-01T18:30:00Z").toEpochMilli(),
            iconUrl = null
        )
        every {
            programmeDao.getByChannelAndTimeRange(
                "BBC1.uk",
                now.toEpochMilli(),
                now.plusSeconds(7200).toEpochMilli()
            )
        } returns flowOf(listOf(entity))

        val programmes = repository.getEpgForChannel(
            "BBC1.uk",
            now,
            now.plusSeconds(7200)
        )

        val result = programmes.first()
        assertEquals(1, result.size)
        assertEquals("News", result[0].title)
    }

    @Test
    fun `clear stale EPG deletes old programmes`() = runTest {
        val before = Instant.parse("2024-01-01T00:00:00Z")

        repository.clearStaleEpg(before)

        coVerify { programmeDao.deleteStale(before.toEpochMilli()) }
    }
}
