package ru.blays.preferences.internal

import androidx.datastore.core.InterProcessCoordinator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias IOException = java.io.IOException

internal class SingleProcessCoordinator: InterProcessCoordinator {
    private val mutex = Mutex()
    private val version = AtomicInt(0)

    override val updateNotifications: Flow<Unit> = flow {}

    // run block with the exclusive lock
    override suspend fun <T> lock(block: suspend () -> T): T {
        return mutex.withLock {
            block()
        }
    }

    // run block with an attempt to get the exclusive lock, still run even if
    // attempt fails. Pass a boolean to indicate if the attempt succeeds.
    override suspend fun <T> tryLock(block: suspend (Boolean) -> T): T {
        return mutex.withTryLock {
            block(it)
        }
    }

    // get the current version
    override suspend fun getVersion(): Int = version.get()

    // increment version and return the new one
    override suspend fun incrementAndGetVersion(): Int = version.incrementAndGet()
}

internal class AtomicInt(initialValue: Int) {
    private val delegate: AtomicInteger = AtomicInteger(initialValue)

    fun getAndIncrement(): Int = delegate.getAndIncrement()
    fun decrementAndGet(): Int = delegate.decrementAndGet()
    fun get(): Int = delegate.get()
    fun incrementAndGet(): Int = delegate.incrementAndGet()
}

internal class AtomicBoolean (initialValue: Boolean) {
    private val delegate: AtomicBoolean = AtomicBoolean(initialValue)

    fun get(): Boolean = delegate.get()

    fun set(value: Boolean) {
        delegate.set(value)
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun <R> Mutex.withTryLock(owner: Any? = null, block: (Boolean) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val locked: Boolean = tryLock(owner)
    try {
        return block(locked)
    } finally {
        if (locked) {
            unlock(owner)
        }
    }
}