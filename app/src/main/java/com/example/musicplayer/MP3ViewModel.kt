package com.example.musicplayer

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.MP3File
import kotlinx.coroutines.launch

class MP3ViewModel(private val repository: MP3Repository): ViewModel() {

    private val _mp3Files = mutableStateOf<List<MP3File>>(emptyList())
    val mp3Files: State<List<MP3File>> = _mp3Files

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _isPlaying = mutableStateOf(false)
    val isPlaying: State<Boolean> = _isPlaying

    private val _mediaPlayer = mutableStateOf(MediaPlayer())
    val mediaPlayer: State<MediaPlayer> = _mediaPlayer

    private val _currentPlayingSong = mutableStateOf<MP3File?>(null)
    val currentPlayingSong: State<MP3File?> = _currentPlayingSong

    private val _currentPlayingIndex = mutableIntStateOf(0)
    val currentPlayingIndex: State<Int> = _currentPlayingIndex

    fun getMusic() {
        viewModelScope.launch {
            val files = repository.getMP3Files()

            if (files.isEmpty()) {
                _error.value = "No se han podido encontrar canciones"
                _mp3Files.value = emptyList()
            } else {
                _mp3Files.value = files
                _error.value = null
            }

        }
    }

    fun playSong(index: Int, context: Context) {
        _isPlaying.value = true


        if (_currentPlayingIndex.intValue == index && !_mediaPlayer.value.isPlaying) {
            _mediaPlayer.value.start()
            return
        }


        _currentPlayingSong.value = _mp3Files.value[index]
        _currentPlayingIndex.intValue = index


        _mediaPlayer.value.release()

        _currentPlayingSong.value?.let { song ->
            _mediaPlayer.value = MediaPlayer.create(context, song.uri).apply {
                start()
                setOnCompletionListener {
                    val nextIndex = (index + 1) % _mp3Files.value.size
                    playSong(nextIndex, context)
                }
            }
        } ?: run {
            _error.value = "No se ha podido reproducir la canción"
        }
    }

    fun pauseSong() {
        if (_mediaPlayer.value.isPlaying) {
            _mediaPlayer.value.pause()
            _isPlaying.value = false
        }
    }
}