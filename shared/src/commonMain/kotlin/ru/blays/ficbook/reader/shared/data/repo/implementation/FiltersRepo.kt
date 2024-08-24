package ru.blays.ficbook.reader.shared.data.repo.implementation

import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import ru.blays.ficbook.reader.shared.data.realm.entity.blacklist.BlacklistAuthorEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.blacklist.BlacklistDirectionEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.blacklist.BlacklistFandomEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.blacklist.BlacklistTagEntity
import ru.blays.ficbook.reader.shared.data.repo.declaration.FanficFilter
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo

internal class FiltersRepo(
    private val realm: Realm
): IFiltersRepo {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override val authorsBlacklist: StateFlow<List<String>> = runBlocking {
        realm.query<BlacklistAuthorEntity>()
            .asFlow()
            .map { it.list.map(BlacklistAuthorEntity::authorName) }
            .stateIn(scope = scope)
    }
    override val fandomsBlacklist: StateFlow<List<String>> = runBlocking {
        realm.query<BlacklistFandomEntity>()
            .asFlow()
            .map { it.list.map(BlacklistFandomEntity::fandomName) }
            .stateIn(scope)
    }
    override val tagsBlacklist: StateFlow<List<String>> = runBlocking {
        realm.query<BlacklistTagEntity>()
            .asFlow()
            .map { it.list.map(BlacklistTagEntity::tagName) }
            .stateIn(scope)
    }
    override val directionsBlacklist: StateFlow<List<String>> = runBlocking {
        realm.query<BlacklistDirectionEntity>()
            .asFlow()
            .map { it.list.map(BlacklistDirectionEntity::direction) }
            .stateIn(scope)
    }

    override fun getFilter(): FanficFilter = FanficFilter { fanfic ->
        if(fanfic.author.name in authorsBlacklist.value) {
            return@FanficFilter false
        }
        if(fanfic.tags.any { it.name in tagsBlacklist.value }) {
            return@FanficFilter false
        }
        if(fanfic.fandoms.any { it.name in fandomsBlacklist.value }) {
            return@FanficFilter false
        }
        if(fanfic.status.direction.name in directionsBlacklist.value) {
            return@FanficFilter false
        }
        return@FanficFilter true
    }

    override suspend fun addAuthorToBlacklist(authorName: String) {
        val alreadyAdded = realm
            .query<BlacklistAuthorEntity>("authorName == $0", authorName)
            .count()
            .find() > 0

        if(!alreadyAdded) {
            realm.write {
                val entity = BlacklistAuthorEntity(
                    authorName = authorName
                )
                copyToRealm(entity)
            }
        }
    }
    override suspend fun removeAuthorFromBlacklist(authorName: String) {
        val entity = realm
            .query<BlacklistAuthorEntity>("authorName == $0", authorName)
            .first()
            .find()

        realm.write {
            if(entity != null) {
                findLatest(entity)?.let { liveEntity ->
                    delete(liveEntity)
                }
            }
        }
    }

    override suspend fun addFandomToBlacklist(fandomName: String) {
        val alreadyAdded = realm
            .query<BlacklistFandomEntity>("fandomName == $0", fandomName)
            .count()
            .find() > 0

        if(!alreadyAdded) {
            realm.write {
                val entity = BlacklistFandomEntity(
                    fandomName = fandomName
                )
                copyToRealm(entity)
            }
        }
    }

    override suspend fun removeFandomFromBlacklist(fandomName: String) {
        val entity = realm
            .query<BlacklistFandomEntity>("fandomName == $0", fandomName)
            .first()
            .find()

        realm.write {
            if(entity != null) {
                findLatest(entity)?.let { liveEntity ->
                    delete(liveEntity)
                }
            }
        }
    }

    override suspend fun addTagToBlacklist(tagName: String) {
        val alreadyAdded = realm
            .query<BlacklistTagEntity>("tagName == $0", tagName)
            .count()
            .find() > 0

        if(!alreadyAdded) {
            realm.write {
                val entity = BlacklistTagEntity(
                    tagName = tagName
                )
                copyToRealm(entity)
            }
        }
    }

    override suspend fun removeTagFromBlacklist(tagName: String) {
        val entity = realm
            .query<BlacklistTagEntity>("tagName == $0", tagName)
            .first()
            .find()

        realm.write {
            if(entity != null) {
                findLatest(entity)?.let { liveEntity ->
                    delete(liveEntity)
                }
            }
        }
    }

    override suspend fun addDirectionToBlacklist(direction: String) {
        val alreadyAdded = realm
            .query<BlacklistDirectionEntity>("direction == $0", direction)
            .count()
            .find() > 0

        if(!alreadyAdded) {
            realm.write {
                val entity = BlacklistDirectionEntity(
                    direction = direction
                )
                copyToRealm(entity)
            }
        }
    }

    override suspend fun removeDirectionFromBlacklist(direction: String) {
        val entity = realm
            .query<BlacklistDirectionEntity>("direction == $0", direction)
            .first()
            .find()

        realm.write {
            if(entity != null) {
                findLatest(entity)?.let { liveEntity ->
                    delete(liveEntity)
                }
            }
        }
    }
}