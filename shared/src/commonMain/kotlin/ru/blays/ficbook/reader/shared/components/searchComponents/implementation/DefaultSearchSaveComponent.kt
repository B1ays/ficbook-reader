package ru.blays.ficbook.reader.shared.components.searchComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId
import ru.blays.ficbook.reader.shared.components.searchComponents.declaration.SearchSaveComponent
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.SnackbarHost
import ru.blays.ficbook.reader.shared.data.SearchParamsEntityShortcut
import ru.blays.ficbook.reader.shared.data.realm.entity.SearchParamsEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.toShortcut
import ru.blays.ficbook.reader.shared.di.injectRealm

class DefaultSearchSaveComponent(
    componentContext: ComponentContext,
    private val onSelect: (SearchParamsEntity) -> Unit,
    private val createEntity: (name: String, description: String) -> SearchParamsEntity
) : SearchSaveComponent, ComponentContext by componentContext {
    private val _state = MutableValue(
        SearchSaveComponent.State(
            saved = emptyList()
        )
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val realm by injectRealm()

    override val state get() = _state

    override fun save(
        name: String,
        description: String
    ) {
        coroutineScope.launch {
            val entity = createEntity(name, description)
            realm.write {
                copyToRealm(entity)
            }
            _state.update {
                val shortcut = entity.toShortcut()
                it.copy(saved = it.saved + shortcut)
            }
            SnackbarHost.showMessage(
                message = "Поиск $name сохранён",
            )
        }
    }

    override fun delete(shortcut: SearchParamsEntityShortcut) {
        coroutineScope.launch {
            val objectId = try {
                ObjectId(shortcut.idHex)
            } catch (_: Exception) {
                null
            }
            if (objectId == null) return@launch

            val success = realm.write {
                val entity = query(
                    clazz = SearchParamsEntity::class,
                    query = "_id == $0",
                    objectId
                ).first().find()
                if(entity == null) return@write false
                delete(entity)
                return@write true
            }
            if (success) {
                _state.update {
                    it.copy(saved = it.saved - shortcut)
                }
                SnackbarHost.showMessage(
                    message = "Поиск ${shortcut.name} удалён",
                )
            }
        }
    }

    override fun select(shortcut: SearchParamsEntityShortcut) {
        val objectId = try {
            ObjectId(shortcut.idHex)
        } catch (_: Exception) {
            null
        }
        if (objectId == null) return
        val entity = realm.query(
            clazz = SearchParamsEntity::class,
            query = "_id == $0",
            objectId
        ).first().find()
        entity?.let(onSelect)
    }

    override fun update(
        shortcut: SearchParamsEntityShortcut,
        newName: String,
        newDescription: String,
        updateParams: Boolean
    ) {
        coroutineScope.launch {
            val index = state.value.saved.indexOf(shortcut)
            val updatedShortcut = shortcut.copy(
                name = newName,
                description = newDescription
            )

            val objectId = try {
                ObjectId(shortcut.idHex)
            } catch (_: Exception) {
                null
            }
            if (objectId == null) return@launch
            realm.write {
                query(
                    clazz = SearchParamsEntity::class,
                    query = "_id == $0",
                    objectId
                ).first().find()?.let { liveEntity ->
                    if(updateParams) {
                        val newEntity = createEntity(newName, newDescription)
                        newEntity._id = liveEntity._id
                        copyToRealm(newEntity, UpdatePolicy.ALL)
                    } else {
                        liveEntity.name = newName
                        liveEntity.description = newDescription
                    }
                }
            }

            if(index == -1) {
                _state.update {
                    it.copy(
                        saved = it.saved + updatedShortcut
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        saved = it.saved.toMutableList().apply {
                            this[index] = updatedShortcut
                        }
                    )
                }
            }
        }
    }

    private fun loadSavedSearches() {
        coroutineScope.launch {
            val saved = realm.query(SearchParamsEntity::class).find()
            val shortcuts = saved.map(SearchParamsEntity::toShortcut)
            _state.update {
                it.copy(saved = shortcuts)
            }
        }
    }

    init { loadSavedSearches() }
}