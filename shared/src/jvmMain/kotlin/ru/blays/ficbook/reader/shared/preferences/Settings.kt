package ru.blays.ficbook.reader.shared.preferences

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences


val preferences: Preferences = Preferences.userRoot()

actual val settings: ObservableSettings = PreferencesSettings(preferences)