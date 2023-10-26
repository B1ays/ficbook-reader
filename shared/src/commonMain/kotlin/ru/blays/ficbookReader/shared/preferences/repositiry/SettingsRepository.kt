package ru.blays.ficbookReader.shared.preferences.repositiry

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.*
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalSettingsApi::class)
class SettingsRepository(
    private val settings: ObservableSettings,
): ISettingsRepository {
    private fun <T : Any, R : Flow<T>> getSettingsFlow(
        key: ISettingsRepository.SettingsKey<T>,
        defaultValue: T
    ): R = when (key) {
        is ISettingsRepository.SettingsKey.BooleanKey -> settings.getBooleanFlow(key.key, defaultValue as Boolean)
        is ISettingsRepository.SettingsKey.IntKey -> settings.getIntFlow(key.key, defaultValue as Int)
        is ISettingsRepository.SettingsKey.LongKey -> settings.getLongFlow(key.key, defaultValue as Long)
        is ISettingsRepository.SettingsKey.FloatKey -> settings.getFloatFlow(key.key, defaultValue as Float)
        is ISettingsRepository.SettingsKey.DoubleKey -> settings.getDoubleFlow(key.key, defaultValue as Double)
        is ISettingsRepository.SettingsKey.StringKey -> settings.getStringFlow(key.key, defaultValue as String)
    } as R

    override fun <T : Any> setValueForKey(key: ISettingsRepository.SettingsKey<T>, value: T) {
        when (key) {
            is ISettingsRepository.SettingsKey.BooleanKey -> settings[key.key] = value as Boolean
            is ISettingsRepository.SettingsKey.IntKey -> settings[key.key] = value as Int
            is ISettingsRepository.SettingsKey.LongKey -> settings[key.key] = value as Long
            is ISettingsRepository.SettingsKey.FloatKey -> settings[key.key] = value as Float
            is ISettingsRepository.SettingsKey.DoubleKey -> settings[key.key] = value as Double
            is ISettingsRepository.SettingsKey.StringKey -> settings[key.key] = value as String
        }
    }

    override fun <T : Any> getValueForKey(key: ISettingsRepository.SettingsKey<T>, defaultValue: T): T = when (key) {
        is ISettingsRepository.SettingsKey.BooleanKey -> settings[key.key, defaultValue as Boolean]
        is ISettingsRepository.SettingsKey.IntKey -> settings[key.key, defaultValue as Int]
        is ISettingsRepository.SettingsKey.LongKey -> settings[key.key, defaultValue as Long]
        is ISettingsRepository.SettingsKey.FloatKey -> settings[key.key, defaultValue as Float]
        is ISettingsRepository.SettingsKey.DoubleKey -> settings[key.key, defaultValue as Double]
        is ISettingsRepository.SettingsKey.StringKey -> settings[key.key, defaultValue as String]
    } as T

    override fun <T : Any> getDelegate(
        key: ISettingsRepository.SettingsKey<T>,
        defaultValue: T
    ): ISettingsRepository.SettingsDelegate<T> = ISettingsRepository.SettingsDelegate(
        get = {
            getValueForKey(key, defaultValue)
        },
        set = { value ->
            setValueForKey(key, value)
        }
    )

    override fun <T : Any, R : Flow<T>> getFlowDelegate(
        key: ISettingsRepository.SettingsKey<T>,
        defaultValue: T
    ): ISettingsRepository.SettingsFlowDelegate<R> = ISettingsRepository.SettingsFlowDelegate {
        getSettingsFlow(key, defaultValue)
    }
}