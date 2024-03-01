package ru.blays.ficbook.reader.shared.preferences.repositiry

import kotlinx.serialization.KSerializer
import kotlin.properties.ReadWriteProperty

interface ISettingsJsonRepository {
    fun <T: Any> getDelegate(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>
    ): ReadWriteProperty<Any?, T>
}