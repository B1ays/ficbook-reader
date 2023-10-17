package ru.blays.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import ru.blays.preferences.internal.dataFolderPath
import java.io.File

actual fun createDataStore(): androidx.datastore.core.DataStore<Preferences> {
    return PreferenceDataStoreFactory.create { File(dataFolderPath, "Preferences.preferences_pb")  }
}