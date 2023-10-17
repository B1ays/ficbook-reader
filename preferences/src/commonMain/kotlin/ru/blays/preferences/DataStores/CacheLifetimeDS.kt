package ru.blays.preferences.DataStores


import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey

class CacheLifetimeDS internal constructor(dataStore: DataStore<Preferences>): BaseDataStore<Long>(dataStore) {
    override val KEY: Preferences.Key<Long> = longPreferencesKey("CacheLifetimeLong")
    override val DEFAULT_VALUE: Long = 6L

}