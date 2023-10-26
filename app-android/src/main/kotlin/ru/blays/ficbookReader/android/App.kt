package ru.blays.ficbookReader.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.blays.ficbookReader.shared.di.ficbookApiModule
import ru.blays.ficbookReader.shared.di.realmModule
import ru.blays.ficbookReader.shared.di.repositoryModule

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        println("Start application")
        startKoin {
            androidContext(this@App)
            modules(
                realmModule,
                ficbookApiModule,
                repositoryModule
            )
        }
    }
}