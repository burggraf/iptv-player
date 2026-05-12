package com.iptvplayer.domain.repository

import com.iptvplayer.domain.model.AppSettings
import com.iptvplayer.domain.model.BufferSize
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateSettings(update: AppSettings.() -> AppSettings)
}
