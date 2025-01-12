package com.example.musicplayer.data

import android.net.Uri

data class MP3File(val uri: Uri, val name: String, val artist: String?, val album: String?, val isFromWhatsapp: Boolean)
