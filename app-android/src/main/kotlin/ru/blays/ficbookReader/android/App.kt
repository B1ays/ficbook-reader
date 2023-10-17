package ru.blays.ficbookReader.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.blays.ficbookReader.shared.data.di.ficbookApiModule
import ru.blays.ficbookReader.shared.data.di.realmModule
import ru.blays.preferences.DI.preferencesModule

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        println("Start application")
        startKoin {
            androidContext(this@App)
            modules(
                preferencesModule,
                realmModule,
                ficbookApiModule
            )
        }
    }
}