package com.example.musicplayer.ui.mvvm

import android.content.Context
import com.example.musicplayer.data.MP3File
import com.example.musicplayer.utils.MP3Scanner
import javax.inject.Inject

class MP3Repository @Inject constructor(context: Context) {
    private val mp3Scanner = MP3Scanner(context)

    suspend fun getMP3Files(): List<MP3File> {
        return mp3Scanner.scanForMP3Files()
    }
}