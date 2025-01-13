package com.example.musicplayer.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.musicplayer.data.MP3File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MP3Scanner(private val context: Context) {
    suspend fun scanForMP3Files(): List<MP3File> = withContext(Dispatchers.IO) {
        val mp3Files = mutableListOf<MP3File>()
        try {
            mp3Files.addAll(scanMediaStore())
            Log.d("MP3Scanner", "Total MP3 files found: ${mp3Files.size}")
        } catch (e: Exception) {
            Log.e("MP3Scanner", "Error scanning for MP3 files", e)
        }
        mp3Files
    }

    private suspend fun scanMediaStore(): List<MP3File> = withContext(Dispatchers.IO) {
        val mp3Files = mutableListOf<MP3File>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("audio/mpeg")
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val artist = cursor.getString(artistColumn)
                    val album = cursor.getString(albumColumn)
                    val duration = cursor.getInt(durationColumn)
                    val contentUri = Uri.withAppendedPath(collection, id.toString())
                    mp3Files.add(MP3File(contentUri, name, duration, artist, album, album == "WhatsApp Audio"))
                }
            }
            Log.d("MP3Scanner", "MP3 files found: ${mp3Files.size}")
        } catch (e: Exception) {
            Log.e("MP3Scanner", "Error scanning for MP3 files", e)
        }
        mp3Files
    }
}

