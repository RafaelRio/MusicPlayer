package com.example.musicplayer.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.musicplayer.R
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
    var filteredList by remember { mutableStateOf(emptyList<MP3File>()) }

    LaunchedEffect(mp3List) {
        filteredList = mp3List
    }

    LaunchedEffect(Unit) {
        viewModel.initPlayer(context)
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
            ), actions = {
                Icon(imageVector = Icons.Filled.Search,
                    contentDescription = "", modifier = Modifier.clickable {
//                        filteredList = mp3List.filter {
//                            it.name.contains("virgen", ignoreCase = true)
//
//                        }
//                        viewModel.updateList(filteredList)
                    })
            }
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
                itemsIndexed(filteredList) { _, file ->
                    MP3ListItem(file, isPlaying, onClick = {
                        val originalIndex = mp3List.indexOf(file)
                        if (originalIndex != -1) {
                            viewModel.playSong(originalIndex, context)
                        }
                    })
                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            BottomPlayer(
                currentPlayingSong = currentPlayingSong,
                currentIndex = currentIndex,
                viewModel = viewModel,
                mp3List = filteredList,
                context = context,
                isPlaying = isPlaying
            )
        }
    }

}

@Composable
fun BottomPlayer(
    currentPlayingSong: MP3File?,
    currentIndex: Int,
    viewModel: MP3ViewModel,
    mp3List: List<MP3File>,
    context: Context,
    isPlaying: Boolean
) {
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
                .fillMaxWidth().padding(horizontal = 10.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {
            if (file.image != null) {
                AsyncImage(
                    model = file.image,
                    contentDescription = "",
                    modifier = Modifier.weight(0.2f)
                )
            } else {
                file.isFromWhatsapp.let { whatsapp ->
                    if (whatsapp) {
                        AsyncImage(
                            model = R.drawable.ic_whatsapp,
                            contentDescription = "",
                            modifier = Modifier.weight(0.2f)
                        )
                    } else {
                        AsyncImage(
                            model = R.drawable.ic_music,
                            contentDescription = "",
                            modifier = Modifier.weight(0.2f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).padding(10.dp)) {
                Text(
                    text = file.name.dropLast(4),
                    style = MaterialTheme.typography.titleSmall,
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