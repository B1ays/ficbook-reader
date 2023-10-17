package ru.blays.preferences.DataStores


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

class CustomColorValueDS internal constructor(dataStore: DataStore<Preferences>): BaseDataStore<Int>(dataStore) {
    override val KEY: Preferences.Key<Int> = intPreferencesKey("CustomColorARGB")
    override val DEFAULT_VALUE: Int = 0
}