package com.example.musicplayer.data

import android.graphics.Bitmap
import android.net.Uri

data class MP3File(
    val uri: Uri,
    val name: String,
    val duration: Int,
    val artist: String?,
    val album: String?,
    val isFromWhatsapp: Boolean,
    val image: Bitmap?
)
