package ru.blays.preferences.DataStores

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

class ColorAccentIndexDS internal constructor(dataStore: DataStore<Preferences>): BaseDataStore<Int>(dataStore) {
    override val KEY: Preferences.Key<Int> = intPreferencesKey("colorAccentIndex")
    override val DEFAULT_VALUE: Int = 1
}