package com.iptvplayer.data.repository

import android.content.Context
import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
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

    private var player: ExoPlayer? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    private val _currentChannel = MutableStateFlow<Channel?>(null)

    private var retryCount = 0
    private var pendingChannel: Channel? = null

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> {
                    if (_playbackState.value !is PlaybackState.Error) {
                        _playbackState.value = PlaybackState.Buffering
                    }
                }
                Player.STATE_READY -> {
                    retryCount = 0
                    _playbackState.value = PlaybackState.Playing(
                        position = player?.currentPosition ?: 0L,
                        duration = player?.duration ?: 0L
                    )
                }
                Player.STATE_IDLE -> {
                    _playbackState.value = PlaybackState.Idle
                }
                Player.STATE_ENDED -> {
                    _playbackState.value = PlaybackState.Idle
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying && _playbackState.value is PlaybackState.Buffering) {
                _playbackState.value = PlaybackState.Playing(
                    position = player?.currentPosition ?: 0L,
                    duration = player?.duration ?: 0L
                )
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val channelName = _currentChannel.value?.name ?: "Unknown"
            val isRecoverable = retryCount < MAX_RETRIES

            _playbackState.value = PlaybackState.Error(
                message = "$channelName: ${error.errorCodeName}",
                recoverable = isRecoverable
            )

            if (isRecoverable && pendingChannel != null) {
                retryCount++
                pendingChannel?.let { channel ->
                    android.util.Log.w(TAG, "Retry $retryCount for ${channel.name}")
                    prepareAndPlay(channel)
                }
            }
        }
    }

    override fun getPlayer(): Player {
        if (player == null) {
            player = ExoPlayer.Builder(context)
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
                .also {
                    it.addListener(playerListener)
                }
        }
        return player!!
    }

    override fun releasePlayer() {
        player?.removeListener(playerListener)
        player?.release()
        player = null
        _playbackState.value = PlaybackState.Idle
        _currentChannel.value = null
    }

    override suspend fun playChannel(channel: Channel): AppResult<Unit> = runCatchingSuspend {
        _currentChannel.value = channel
        pendingChannel = channel
        retryCount = 0
        _playbackState.value = PlaybackState.Loading
        prepareAndPlay(channel)
        Unit
    }

    private fun prepareAndPlay(channel: Channel) {
        val exoPlayer = getPlayer() as ExoPlayer
        val mediaItem = buildMediaItem(channel)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    override fun getPlaybackState(): Flow<PlaybackState> = _playbackState.asStateFlow()

    override fun getCurrentPosition(): Long = player?.currentPosition ?: 0L

    override fun getDuration(): Long = player?.duration ?: 0L

    companion object {
        private const val TAG = "PlaybackRepository"
        private const val MAX_RETRIES = 2

        fun buildMediaItem(channel: Channel): MediaItem {
            val uri = Uri.parse(channel.streamUrl)
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
