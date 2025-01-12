package com.example.musicplayer.di

import com.example.musicplayer.MP3Repository
import com.example.musicplayer.MP3ViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    // Proveer el MP3Repository
    single { MP3Repository(get()) }

    // Proveer el MusicViewModel
    viewModel { MP3ViewModel(get()) }
}