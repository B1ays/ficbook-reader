package ru.blays.ficbookReader.shared.preferences.repositiry

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.serialization.serializedValue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlin.properties.ReadWriteProperty

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class SettingsJsonRepository (
    private val settings: ObservableSettings,
): ISettingsJsonRepository {
    override fun <T : Any> getDelegate(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>
    ): ReadWriteProperty<Any?, T> = settings.serializedValue(
        serializer = serializer,
        key = key,
        defaultValue = defaultValue
    )
}