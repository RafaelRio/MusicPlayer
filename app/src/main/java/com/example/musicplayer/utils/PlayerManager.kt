package com.example.musicplayer.utils

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null

    fun getExoPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
        return exoPlayer!!
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }
}