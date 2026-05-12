package com.iptvplayer.domain.repository

import com.iptvplayer.core.AppResult
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    suspend fun toggleFavorite(channelId: String): AppResult<Unit>
    fun isFavorite(channelId: String): Flow<Boolean>
    fun getFavorites(): Flow<List<String>>
}
