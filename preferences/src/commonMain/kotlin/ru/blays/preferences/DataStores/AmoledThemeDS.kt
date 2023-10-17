package ru.blays.preferences.DataStores

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

class AmoledThemeDS internal constructor(dataStore: DataStore<Preferences>): BaseDataStore<Boolean>(dataStore) {
    override val KEY: Preferences.Key<Boolean> = booleanPreferencesKey("amoledTheme")
    override val DEFAULT_VALUE: Boolean = false
}