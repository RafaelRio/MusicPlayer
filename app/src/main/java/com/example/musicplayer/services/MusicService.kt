package com.example.musicplayer.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerNotificationManager
import com.example.musicplayer.MainActivity
import com.example.musicplayer.utils.PlayerManager

@UnstableApi
class MusicService : Service() {

    private lateinit var player: ExoPlayer
    private lateinit var playerNotificationManager: PlayerNotificationManager

    override fun onCreate() {
        super.onCreate()

        player = PlayerManager.getExoPlayer(this)
        // Iniciar el reproductor
        // Crear el canal de notificaciones si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Configurar el PlayerNotificationManager
        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        ).setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
            override fun getCurrentContentTitle(player: Player): CharSequence {
                return player.mediaMetadata.title ?: "Desconocido"
            }

            override fun createCurrentContentIntent(player: Player): PendingIntent? {
                val intent = Intent(this@MusicService, MainActivity::class.java)
                return PendingIntent.getActivity(
                    this@MusicService,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }

            override fun getCurrentContentText(player: Player): CharSequence? {
                return player.mediaMetadata.artist ?: "Artista desconocido"
            }

            override fun getCurrentLargeIcon(
                player: Player,
                callback: PlayerNotificationManager.BitmapCallback
            ): Bitmap? {
                return null
            }
        }).build()

        // Vincular el reproductor a la notificación
        playerNotificationManager.setPlayer(player)
        playerNotificationManager.setUseStopAction(true)
        playerNotificationManager.setUsePlayPauseActions(true)

        // Poner el servicio en primer plano mostrando la notificación
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Reproducir la canción si ya está configurada
        player.prepare()
        player.play()
    }

    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(player.mediaMetadata.title ?: "Desconocido")
            .setContentText(player.mediaMetadata.artist ?: "Artista desconocido")
            .setSmallIcon(androidx.media3.ui.R.drawable.exo_notification_small_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerNotificationManager.setPlayer(null)
        PlayerManager.releasePlayer()
        player.release()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        //val player = PlayerManager.getExoPlayer(this)
        // Obtener la canción desde el Intent
        val songUri = intent?.getStringExtra("song_uri")?.let { Uri.parse(it) }
        val songTitle = intent?.getStringExtra("song_title") ?: "Desconocido"
        val songArtist = intent?.getStringExtra("song_artist") ?: "Desconocido"

        // Configurar el MediaItem del ExoPlayer con la canción recibida
        songUri?.let {
            val mediaItem = MediaItem.Builder()
                .setUri(it)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(songTitle)
                        .setArtist(songArtist)
                        .build()
                )
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            // Poner el servicio en primer plano con la notificación
            val notification = buildNotification()
            startForeground(NOTIFICATION_ID, notification)
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1
    }
}