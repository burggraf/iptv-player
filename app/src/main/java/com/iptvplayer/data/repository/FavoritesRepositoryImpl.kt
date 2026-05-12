package com.iptvplayer.data.repository

import com.iptvplayer.core.AppResult
import com.iptvplayer.core.runCatchingSuspend
import com.iptvplayer.data.local.database.FavoriteDao
import com.iptvplayer.data.local.entities.FavoriteChannelEntity
import com.iptvplayer.core.DispatcherProvider
import com.iptvplayer.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FavoritesRepositoryImpl(
    private val favoriteDao: FavoriteDao,
    private val dispatcherProvider: DispatcherProvider
) : FavoritesRepository {

    override suspend fun toggleFavorite(channelId: String): AppResult<Unit> = runCatchingSuspend {
        withContext(dispatcherProvider.io) {
            favoriteDao.insert(
                FavoriteChannelEntity(
                    channelId = channelId,
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override fun isFavorite(channelId: String): Flow<Boolean> =
        favoriteDao.isFavorite(channelId)

    override fun getFavorites(): Flow<List<String>> =
        favoriteDao.getFavorites()
}
