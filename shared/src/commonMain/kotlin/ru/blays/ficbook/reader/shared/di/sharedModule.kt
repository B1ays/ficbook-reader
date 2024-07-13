package ru.blays.ficbook.reader.shared.di

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.binds
import org.koin.dsl.module
import ru.blays.ficbook.api.di.ficbookApiModule
import ru.blays.ficbook.reader.shared.preferences.settings

val sharedModule = module {
    single { settings } binds arrayOf(Settings::class, ObservableSettings::class)
    includes(
        okHttpModule,
        ficbookApiModule,
        repositoryModule,
        realmModule
    )
}