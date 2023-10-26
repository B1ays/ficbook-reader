package ru.blays.ficbookReader.shared.preferences.repositiry

import io.realm.kotlin.internal.platform.runBlocking
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

interface ISettingsRepository {
    fun <T: Any> setValueForKey(key: SettingsKey<T>, value: T)
    fun <T: Any> getValueForKey(key: SettingsKey<T>, defaultValue: T): T

    fun <T : Any> getDelegate(key: SettingsKey<T>, defaultValue: T): SettingsDelegate<T>
    fun <T : Any, R: Flow<T>> getFlowDelegate(key: SettingsKey<T>, defaultValue: T): SettingsFlowDelegate<R>

    class SettingsDelegate<T: Any>(
        private val get: () -> T,
        private val set: (value: T) -> Unit
    ) {
        operator fun getValue(thisRef: Any, property: KProperty<*>): T = get()
        operator fun setValue(thisRef: Any, property: KProperty<*>, t: T) = set(t)
    }

    class SettingsFlowDelegate<T: Flow<*>>(
        private val get: () -> T
    ) {
        operator fun getValue(thisRef: Any, property: KProperty<*>): T = get()

        companion object {
            fun <T> Flow<T>.lastSynchronously(): T = runBlocking {
                var job: Job? = null
                var result: T? = null
                job = launch {
                    collect {
                        result = it
                        job?.cancel()
                        return@collect
                    }
                }
                job.join()
                result!!
            }
        }
    }

    sealed class SettingsKey <T: Any> {
        data class BooleanKey(val key: String) : SettingsKey<Boolean>()
        data class IntKey(val key: String) : SettingsKey<Int>()
        data class LongKey(val key: String) : SettingsKey<Long>()
        data class FloatKey(val key: String) : SettingsKey<Float>()
        data class DoubleKey(val key: String) : SettingsKey<DoubleKey>()
        data class StringKey(val key: String) : SettingsKey<String>()
    }

    companion object {
        fun booleanKey(key: String): SettingsKey.BooleanKey = SettingsKey.BooleanKey(key)
        fun intKey(key: String): SettingsKey.IntKey = SettingsKey.IntKey(key)
        fun longKey(key: String): SettingsKey.LongKey = SettingsKey.LongKey(key)
        fun floatKey(key: String): SettingsKey.FloatKey = SettingsKey.FloatKey(key)
        fun doubleKey(key: String): SettingsKey.DoubleKey = SettingsKey.DoubleKey(key)
        fun stringKey(key: String): SettingsKey.StringKey = SettingsKey.StringKey(key)
    }
}