package com.example.musicplayer

import android.app.Application
import com.example.musicplayer.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class MP3App: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger() // Opcional: para logs de Koin
            androidContext(this@MP3App) // Proveer el contexto de la aplicación
            modules(appModule) // Registrar los módulos
        }
    }
}