package ru.blays.ficbook.reader.shared.stateHandle

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.essenty.statekeeper.StateKeeper
import com.arkivanov.essenty.statekeeper.StateKeeperOwner
import kotlinx.serialization.KSerializer

@Suppress("FunctionName")
internal inline fun <reified T: Any> StateKeeper.SaveableMutableValue(
    key: String = T::class.java.name,
    serializer: KSerializer<T>,
    initialValue: T
): MutableValue<T> {
    val value = consume(key, serializer) ?: initialValue
    val mutableValue = MutableValue(value)
    register(key, serializer, mutableValue::value)
    return mutableValue
}

@Suppress("FunctionName")
internal inline fun <reified T: Any> StateKeeperOwner.SaveableMutableValue(
    key: String = T::class.java.name,
    serializer: KSerializer<T>,
    initialValue: T
): MutableValue<T> = stateKeeper.SaveableMutableValue(
    key = key,
    serializer = serializer,
    initialValue = initialValue
)