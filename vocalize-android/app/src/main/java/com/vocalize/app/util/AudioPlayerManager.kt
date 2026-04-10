package com.vocalize.app.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Int = 0,
    val duration: Int = 0,
    val currentMemoId: String? = null,
    val playbackSpeed: Float = 1.0f
)

@Singleton
class AudioPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSessionCompat? = null
    var onPositionSave: ((String, Long) -> Unit)? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    init {
        setupMediaSession()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSessionCompat(context, "VocalizeSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { togglePlayPause() }
                override fun onPause() { togglePlayPause() }
                override fun onStop() { release() }
                override fun onSeekTo(pos: Long) { seekTo(pos.toInt()) }
                override fun onSkipToNext() {}
                override fun onSkipToPrevious() {}
            })
            isActive = true
        }
    }

    fun prepareAndPlay(filePath: String, memoId: String, startPositionMs: Long = 0L) {
        release()
        val file = File(filePath)
        if (!file.exists()) return

        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            if (startPositionMs > 0) seekTo(startPositionMs.toInt())
            start()
            setOnCompletionListener {
                onPositionSave?.invoke(memoId, 0L)
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    currentPosition = 0,
                    currentMemoId = null
                )
                updateMediaSessionState(false)
            }
            _playbackState.value = PlaybackState(
                isPlaying = true,
                currentPosition = startPositionMs.toInt(),
                duration = this.duration,
                currentMemoId = memoId,
                playbackSpeed = _playbackState.value.playbackSpeed
            )
        }
        applySpeed(_playbackState.value.playbackSpeed)
        updateMediaSessionState(true)
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                updateMediaSessionState(false)
                // Save position when pausing
                _playbackState.value.currentMemoId?.let { id ->
                    onPositionSave?.invoke(id, player.currentPosition.toLong())
                }
            } else {
                player.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                updateMediaSessionState(true)
            }
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _playbackState.value = _playbackState.value.copy(currentPosition = positionMs)
    }

    fun setSpeed(speed: Float) {
        _playbackState.value = _playbackState.value.copy(playbackSpeed = speed)
        applySpeed(speed)
    }

    private fun applySpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let { player ->
                    val params = player.playbackParams.setSpeed(speed)
                    player.playbackParams = params
                }
            } catch (_: Exception) {}
        }
    }

    private fun updateMediaSessionState(playing: Boolean) {
        val state = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (playing) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                _playbackState.value.currentPosition.toLong(),
                _playbackState.value.playbackSpeed
            )
            .build()
        mediaSession?.setPlaybackState(state)
    }

    fun getMediaSessionToken(): MediaSessionCompat.Token? = mediaSession?.sessionToken

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun release() {
        // Save position before release
        mediaPlayer?.let { player ->
            _playbackState.value.currentMemoId?.let { id ->
                onPositionSave?.invoke(id, player.currentPosition.toLong())
            }
            if (player.isPlaying) player.stop()
            player.release()
        }
        mediaPlayer = null
        _playbackState.value = PlaybackState()
        updateMediaSessionState(false)
    }

    fun updatePosition() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                _playbackState.value = _playbackState.value.copy(
                    currentPosition = player.currentPosition
                )
            }
        }
    }

    fun destroy() {
        release()
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
    }
}
