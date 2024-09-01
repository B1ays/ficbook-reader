package ru.blays.ficbook.reader.shared.data.repo.declaration

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbook.reader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbook.reader.shared.data.dto.FanficDirection

interface IFiltersRepo {
    val authorsBlacklist: StateFlow<List<String>>
    val fanficsInBlacklist: StateFlow<List<Pair<String, String>>>
    val fandomsBlacklist: StateFlow<List<String>>
    val tagsBlacklist: StateFlow<List<String>>
    val directionsBlacklist: StateFlow<List<FanficDirection>>

    fun getFilter(): FanficFilter

    suspend fun addAuthorToBlacklist(authorName: String)
    suspend fun removeAuthorFromBlacklist(authorName: String)

    suspend fun addFanficToBlacklist(fanficID: String, fanficName: String)
    suspend fun removeFanficFromBlacklist(fanficID: String)

    suspend fun addFandomToBlacklist(fandomName: String)
    suspend fun removeFandomFromBlacklist(fandomName: String)

    suspend fun addTagToBlacklist(tagName: String)
    suspend fun removeTagFromBlacklist(tagName: String)

    suspend fun addDirectionToBlacklist(direction: String)
    suspend fun removeDirectionFromBlacklist(direction: String)
}

fun interface FanficFilter {
    fun filter(fanficCardModel: FanficCardModelStable): Boolean
}