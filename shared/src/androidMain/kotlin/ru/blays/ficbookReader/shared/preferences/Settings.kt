package ru.blays.ficbookReader.shared.preferences

import android.content.Context
import android.content.SharedPreferences
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.java.KoinJavaComponent.get

val sharedPrefs: SharedPreferences = get<Context>(Context::class.java)
    .getSharedPreferences("Prefs", Context.MODE_PRIVATE)

actual val settings: ObservableSettings = SharedPreferencesSettings(sharedPrefs)