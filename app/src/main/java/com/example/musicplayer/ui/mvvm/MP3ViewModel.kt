package com.example.musicplayer.ui.mvvm

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.data.MP3File
import com.example.musicplayer.services.MusicService
import com.example.musicplayer.utils.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MP3ViewModel @Inject constructor(private val repository: MP3Repository) : ViewModel() {

    private val _mp3Files = mutableStateOf<List<MP3File>>(emptyList())
    val mp3Files: State<List<MP3File>> = _mp3Files

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private lateinit var exoPlayer: ExoPlayer
    private var savedPosition: Long = 0

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    private val _currentPlayingSong = mutableStateOf<MP3File?>(null)
    val currentPlayingSong: State<MP3File?> = _currentPlayingSong

    private val _currentPlayingIndex = mutableIntStateOf(0)
    val currentPlayingIndex: State<Int> = _currentPlayingIndex

    fun initPlayer(context: Context) {
        exoPlayer = PlayerManager.getExoPlayer(context)
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> _isPlaying.value = exoPlayer.isPlaying
                    Player.STATE_ENDED -> {
                        savedPosition = 0 // Reiniciar la posición cuando termina
                        val nextIndex = (_currentPlayingIndex.intValue + 1) % _mp3Files.value.size
                        playSong(nextIndex, context)
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    fun getMusic() {
        viewModelScope.launch {
            val files = repository.getMP3Files()
            if (files.isEmpty()) {
                _error.value = "No se han podido encontrar canciones"
            } else {
                _mp3Files.value = files
            }
        }
    }

    fun playSong(index: Int, context: Context) {
        val song = _mp3Files.value.getOrNull(index)
        if (song != null) {
            val isSameSong = _currentPlayingIndex.intValue == index
            _currentPlayingIndex.intValue = index
            _currentPlayingSong.value = song

            if (!isSameSong) {
                // Si es una canción diferente, comenzar desde el principio
                savedPosition = 0
                exoPlayer.setMediaItem(
                    androidx.media3.common.MediaItem.Builder()
                        .setUri(song.uri)
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(song.name)
                                .setArtist(song.artist)
                                .build()
                        ).build()
                )
                exoPlayer.prepare()
                startMusicService(context)
                exoPlayer.play()
            } else {
                // Si es la misma canción, reanudar desde la posición guardada
                startMusicService(context)
                resumeSong()
            }
        } else {
            _error.value = "No se pudo reproducir la canción"
        }
    }

    fun pauseSong() {
        savedPosition = exoPlayer.currentPosition
        exoPlayer.pause()
    }

    private fun resumeSong() {
        exoPlayer.seekTo(savedPosition)
        exoPlayer.play()
    }

    override fun onCleared() {
        super.onCleared()
        // Guardar la última posición antes de liberar el reproductor
        if (::exoPlayer.isInitialized) {
            savedPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
    }

    @OptIn(UnstableApi::class)
    private fun startMusicService(context: Context) {
        val intent = Intent(context, MusicService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}