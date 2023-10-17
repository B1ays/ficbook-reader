package ru.blays.ficbookapi.parsers

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.blays.ficbookapi.dataModels.CookieModel

internal class CookieParser: IDataParser<List<String>, List<CookieModel>> {
    override suspend fun parse(data: List<String>): List<CookieModel> = coroutineScope {
        val models = mutableListOf<CookieModel>()
        data.forEach { rawCookie ->
            rawCookie.split("; ").forEach { cookie ->
                if (cookie.contains('=')) {
                    val name = cookie.substringBefore('=')
                    val value = cookie.substringAfter('=')
                    models += CookieModel(
                        name, value
                    )
                }
            }
        }
        return@coroutineScope models
    }

    override fun parseSynchronously(data: List<String>): StateFlow<List<CookieModel>> {
        val resultFlow = MutableStateFlow<List<CookieModel>>(emptyList())
        launch {
            resultFlow.value = parse(data)
        }
        return resultFlow
    }
}