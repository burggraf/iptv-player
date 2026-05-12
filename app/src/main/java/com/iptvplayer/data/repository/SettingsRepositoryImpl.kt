package com.iptvplayer.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.iptvplayer.domain.model.AppSettings
import com.iptvplayer.domain.model.BufferSize
import com.iptvplayer.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(context: Context) : SettingsRepository {
    private val dataStore = context.settingsDataStore

    override fun getSettings(): Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            epgRefreshIntervalHours = prefs[EPG_REFRESH_INTERVAL] ?: 6,
            pixelPerMinute = prefs[PIXEL_PER_MINUTE] ?: 2,
            channelSwitchDelayMs = prefs[CHANNEL_SWITCH_DELAY]?.toLong() ?: 300L,
            startOnLastChannel = prefs[START_ON_LAST_CHANNEL] ?: true,
            showChannelNumbers = prefs[SHOW_CHANNEL_NUMBERS] ?: true,
            bufferSize = BufferSize.valueOf(prefs[BUFFER_SIZE] ?: BufferSize.MEDIUM.name),
        )
    }

    override suspend fun updateSettings(update: AppSettings.() -> AppSettings) {
        dataStore.edit { prefs ->
            val current = prefs.toSettings()
            val updated = current.update()
            prefs[EPG_REFRESH_INTERVAL] = updated.epgRefreshIntervalHours
            prefs[PIXEL_PER_MINUTE] = updated.pixelPerMinute
            prefs[CHANNEL_SWITCH_DELAY] = updated.channelSwitchDelayMs.toInt()
            prefs[START_ON_LAST_CHANNEL] = updated.startOnLastChannel
            prefs[SHOW_CHANNEL_NUMBERS] = updated.showChannelNumbers
            prefs[BUFFER_SIZE] = updated.bufferSize.name
        }
    }

    private fun Preferences.toSettings(): AppSettings = AppSettings(
        epgRefreshIntervalHours = this[EPG_REFRESH_INTERVAL] ?: 6,
        pixelPerMinute = this[PIXEL_PER_MINUTE] ?: 2,
        channelSwitchDelayMs = this[CHANNEL_SWITCH_DELAY]?.toLong() ?: 300L,
        startOnLastChannel = this[START_ON_LAST_CHANNEL] ?: true,
        showChannelNumbers = this[SHOW_CHANNEL_NUMBERS] ?: true,
        bufferSize = BufferSize.valueOf(this[BUFFER_SIZE] ?: BufferSize.MEDIUM.name),
    )

    companion object {
        private val EPG_REFRESH_INTERVAL = intPreferencesKey("epg_refresh_interval")
        private val PIXEL_PER_MINUTE = intPreferencesKey("pixel_per_minute")
        private val CHANNEL_SWITCH_DELAY = intPreferencesKey("channel_switch_delay")
        private val START_ON_LAST_CHANNEL = booleanPreferencesKey("start_on_last_channel")
        private val SHOW_CHANNEL_NUMBERS = booleanPreferencesKey("show_channel_numbers")
        private val BUFFER_SIZE = stringPreferencesKey("buffer_size")
    }
}
