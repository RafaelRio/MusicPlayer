package com.example.musicplayer.ui.mvvm

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
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
        _currentPlayingSong.value = null
        _currentPlayingIndex.intValue = -1
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

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.e("MP3ViewModel", "Error en ExoPlayer: ${error.message}")
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
            Log.d("MP3ViewModel", "Intentando reproducir canción: ${song.name}, índice: $index")
            Log.d(
                "MP3ViewModel",
                "Canción actual: ${_currentPlayingSong.value?.name}, índice actual: ${_currentPlayingIndex.intValue}"
            )


            // Configurar siempre el MediaItem en el reproductor
            Log.d("MP3ViewModel", "Configurando el MediaItem para la canción: ${song.name}")
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
            if (_currentPlayingSong.value == song) {
                startMusicService(context)
                resumeSong()

            } else {
                exoPlayer.seekTo(0)
                exoPlayer.play()
            }
            // Actualizar siempre el estado del índice y la canción actuales
            _currentPlayingIndex.intValue = index
            _currentPlayingSong.value = song


            // Iniciar la reproducción
            Log.d("MP3ViewModel", "Iniciando la reproducción desde el principio")


            _isPlaying.value = true
        } else {
            Log.d("MP3ViewModel", "No se encontró la canción en el índice: $index")
            _error.value = "No se pudo reproducir la canción"
        }
    }

    fun pauseSong() {
        savedPosition = exoPlayer.currentPosition
        exoPlayer.pause()
        _isPlaying.value = false
    }

    private fun resumeSong() {
        exoPlayer.seekTo(savedPosition)
        exoPlayer.play()
        _isPlaying.value = true
    }

    override fun onCleared() {
        super.onCleared()
        // Guardar la última posición antes de liberar el reproductor
        if (::exoPlayer.isInitialized) {
            savedPosition = exoPlayer.currentPosition
            exoPlayer.release()
        }
    }

    fun updateList(newList: List<MP3File>) {
        _mp3Files.value = newList
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