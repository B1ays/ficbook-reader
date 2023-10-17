package ru.blays.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.java.KoinJavaComponent.get

actual fun createDataStore(): DataStore<Preferences> {
    return get<Context>(Context::class.java).dataStore
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "Preferences"
)


