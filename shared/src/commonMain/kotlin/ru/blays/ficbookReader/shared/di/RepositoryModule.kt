package ru.blays.ficbookReader.shared.di

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsJsonRepository
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbookReader.shared.preferences.repositiry.SettingsJsonRepository
import ru.blays.ficbookReader.shared.preferences.repositiry.SettingsRepository
import ru.blays.ficbookReader.shared.preferences.settings

internal val repositoryModule = module {
    singleOf(::SettingsRepository) bind ISettingsRepository::class
    singleOf(::SettingsJsonRepository) bind ISettingsJsonRepository::class
    single { settings } binds arrayOf(Settings::class, ObservableSettings::class)
}