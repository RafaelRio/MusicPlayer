package com.example.musicplayer

import android.content.Context
import com.example.musicplayer.data.MP3File
import com.example.musicplayer.utils.MP3Scanner

class MP3Repository(context: Context) {
    private val mp3Scanner = MP3Scanner(context)

    suspend fun getMP3Files(): List<MP3File> {
        return mp3Scanner.scanForMP3Files()
    }
}