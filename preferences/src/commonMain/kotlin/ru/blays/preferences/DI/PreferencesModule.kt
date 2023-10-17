package ru.blays.preferences.DI


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.dsl.module
import ru.blays.preferences.DataStores.*
import ru.blays.preferences.createDataStore

val preferencesModule = module {
    single { AmoledThemeDS(get()) }
    single { CacheLifetimeDS(get()) }
    single { ColorAccentIndexDS(get()) }
    single { CustomColorValueDS(get()) }
    single { CustomColorSelectedDS(get()) }
    single { MonetColorsDS(get()) }
    single { ThemeDS(get()) }
    single<DataStore<Preferences>> { createDataStore() }
}