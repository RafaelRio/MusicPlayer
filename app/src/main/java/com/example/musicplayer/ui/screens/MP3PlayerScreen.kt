package com.example.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.data.MP3File
import com.example.musicplayer.ui.mvvm.MP3ViewModel
import kotlin.math.floor


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MP3PlayerScreen(viewModel: MP3ViewModel = hiltViewModel()) {

    val mp3List: List<MP3File> by viewModel.mp3Files
    val isPlaying by viewModel.isPlaying
    val context = LocalContext.current
    val error by viewModel.error
    val currentPlayingSong by viewModel.currentPlayingSong
    val currentIndex by viewModel.currentPlayingIndex

    LaunchedEffect(Unit) {
        viewModel.getMusic()
    }

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing, topBar = {
        TopAppBar(
            title = {
                Text("Reproductor de música")
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {


            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(mp3List) { index, file ->
                    MP3ListItem(file, isPlaying, onClick = {
                        viewModel.playSong(index, context)
                    })
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {

                Box(
                    modifier = Modifier
                        .clipToBounds()
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = currentPlayingSong?.name ?: "No file selected",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (currentIndex > 0) {
                                val newIndex = currentIndex - 1
                                viewModel.playSong(newIndex, context)
                            } else {
                                val newIndex = mp3List.size - 1
                                viewModel.playSong(newIndex, context)
                            }
                        },
                        enabled = currentPlayingSong != null

                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (isPlaying) {
                                viewModel.pauseSong()
                            } else {
                                viewModel.playSong(currentIndex, context)
                            }
                        },
                        enabled = currentPlayingSong != null

                    ) {
                        Icon(
                            imageVector = if (!isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPlaying) "Playing" else "Not playing",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (currentIndex < mp3List.size - 1) {
                                val newIndex = currentIndex + 1
                                viewModel.playSong(newIndex, context)

                            } else {
                                val newIndex = 0
                                viewModel.playSong(newIndex, context)
                            }
                        },
                        enabled = currentPlayingSong != null

                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun MP3ListItem(file: MP3File, isPlaying: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),

            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = file.name.dropLast(4),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${file.artist ?: "Unknown Artist"} - ${file.album ?: "Unknown Album"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = convertTimestampToDuration(file.duration.toLong()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                file.isFromWhatsapp.let { whatsapp ->
                    if (whatsapp) {
                        Text(
                            text = "From Whatsapp",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

            }
        }
    }

}

fun convertTimestampToDuration(position: Long): String {
    val seconds = floor(position / 1E3).toInt()
    val minutes = seconds / 60
    val remainingTimeSeconds = seconds - (minutes * 60)
    return if (position < 0) "--:--" else "%d:%02d".format(minutes, remainingTimeSeconds)
}