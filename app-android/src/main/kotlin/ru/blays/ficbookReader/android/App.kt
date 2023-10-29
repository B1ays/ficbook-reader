package ru.blays.ficbookReader.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.blays.ficbookReader.shared.di.sharedModule

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                sharedModule
            )
        }
    }
}