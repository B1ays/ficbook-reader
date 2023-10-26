package ru.blays.ficbookReader.shared.preferences.repositiry

import kotlinx.serialization.KSerializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface ISettingsJsonRepository {
    fun <T: Any> getDelegate(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>
    ): ReadWriteProperty<Any?, T>

    class Delegate<T: Any>(
        private val get: () -> T,
        private val set: (value: T) -> Unit
    ) {
        operator fun getValue(thisRef: Any, property: KProperty<*>): T = get()
        operator fun setValue(thisRef: Any, property: KProperty<*>, t: T) = set(t)
    }
}