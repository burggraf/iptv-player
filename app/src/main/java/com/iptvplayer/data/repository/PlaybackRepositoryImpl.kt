package com.iptvplayer.data.repository

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import com.iptvplayer.core.AppResult
import com.iptvplayer.core.runCatchingSuspend
import com.iptvplayer.domain.model.Channel
import com.iptvplayer.domain.model.PlaybackState
import com.iptvplayer.domain.repository.PlaybackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackRepositoryImpl(
    private val context: Context
) : PlaybackRepository {

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)

    override fun createPlayer(): Player = ExoPlayer.Builder(context)
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    1500,  // min buffer — fast channel switching
                    5000,  // max buffer
                    500,   // buffer for playback start
                    1000   // buffer after rebuffer
                )
                .build()
        )
        .build()

    override suspend fun prepareChannel(channel: Channel): AppResult<Unit> = runCatchingSuspend {
        // Player preparation delegated to ViewModel; repo provides MediaItem
        Unit
    }

    override fun getPlaybackState(): Flow<PlaybackState> = _playbackState.asStateFlow()

    companion object {
        fun buildMediaItem(channel: Channel): MediaItem {
            val uri = android.net.Uri.parse(channel.streamUrl)
            val mimeType = when {
                channel.streamUrl.endsWith(".m3u8") -> MimeTypes.APPLICATION_M3U8
                channel.streamUrl.endsWith(".mpd") -> MimeTypes.APPLICATION_MPD
                else -> null // Let ExoPlayer sniff
            }
            return MediaItem.Builder()
                .setUri(uri)
                .setMimeType(mimeType)
                .setLiveConfiguration(
                    MediaItem.LiveConfiguration.Builder()
                        .setMaxPlaybackSpeed(1.0f)
                        .build()
                )
                .build()
        }
    }
}
