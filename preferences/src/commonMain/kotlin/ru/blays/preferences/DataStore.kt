package ru.blays.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal expect fun createDataStore(): DataStore<Preferences>