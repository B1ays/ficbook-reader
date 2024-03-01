package ru.blays.ficbook.api.parsers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

interface IDataParser<in I, out O>: CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    suspend fun parse(data: I): O

    fun parseSynchronously(data: I): StateFlow<O?>
}
