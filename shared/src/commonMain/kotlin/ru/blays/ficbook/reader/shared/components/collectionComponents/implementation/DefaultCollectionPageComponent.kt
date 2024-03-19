package ru.blays.ficbook.reader.shared.components.collectionComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.russhwolf.settings.nullableString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionPageComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponentInternal
import ru.blays.ficbook.reader.shared.components.fanficListComponents.implementation.DefaultFanficsListComponent
import ru.blays.ficbook.reader.shared.data.dto.CollectionPageModelStable
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICollectionsRepo
import ru.blays.ficbook.reader.shared.platformUtils.runOnUiThread
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.settings

class DefaultCollectionPageComponent(
    componentContext: ComponentContext,
    private val relativeID: String,
    private val realID: String,
    private val initialDialogConfig: EditCollectionDialogConfig? = null,
    private val output: (output: FanficsListComponent.Output) -> Unit
): CollectionPageComponent, ComponentContext by componentContext {
    private val collectionsRepo: ICollectionsRepo by getKoin().inject()

    private val navigation = SlotNavigation<EditCollectionDialogConfig>()

    private var sortTypeSetting by settings.nullableString(
        key = SettingsKeys.COLLECTION_SORT_TYPE_KEY
    )

    private val _state = MutableValue(
        CollectionPageComponent.State(
            collectionPage = null,
            currentParams = CollectionPageComponent.SelectedSortParams(
                sort = getSavedSortParameter()
            ),
            loading = false,
            error = false,
            errorMessage = null
        )
    )
    private val _fanficsList: FanficsListComponentInternal = DefaultFanficsListComponent(
        componentContext = childContext("fanfics_list"),
        section = createSection(),
        output = output
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val state
        get() = _state
    override val fanficsListComponent: FanficsListComponent
        get() = _fanficsList

    override val editDialog = childSlot(
        source = navigation,
        serializer = EditCollectionDialogConfig.serializer(),
        initialConfiguration = ::initialDialogConfig,
        childFactory = { config, childContext ->
            EditCollectionComponent(
                componentContext = childContext,
                repository = collectionsRepo,
                coroutineScope = coroutineScope,
                config = config,
                onCancel = navigation::dismiss,
                onSuccess = ::onUpdateSuccess
            )
        }
    )

    override fun sendIntent(intent: CollectionPageComponent.Intent) {
        when(intent) {
            is CollectionPageComponent.Intent.ChangeDirection -> {
                _state.update {
                    it.copy(
                        currentParams = it.currentParams.copy(
                            direction = intent.directionCode
                        )
                    )
                }
            }
            is CollectionPageComponent.Intent.ChangeFandom -> {
                _state.update {
                    it.copy(
                        currentParams = it.currentParams.copy(
                            fandom = intent.fandomCode
                        )
                    )
                }
            }
            is CollectionPageComponent.Intent.ChangeSearchText -> {
                _state.update {
                    it.copy(
                        currentParams = it.currentParams.copy(
                            searchText = intent.searchText
                        )
                    )
                }
            }
            is CollectionPageComponent.Intent.ChangeSortType -> {
                _state.update {
                    it.copy(
                        currentParams = it.currentParams.copy(
                            sort = intent.sortTypeCode
                        )
                    )
                }
                sortTypeSetting = intent.sortTypeCode.let { "${it.first}|${it.second}" }
            }
            is CollectionPageComponent.Intent.Search -> {
                _fanficsList.setSection(
                    section = createSection()
                )
            }
            is CollectionPageComponent.Intent.Refresh -> loadPage()
            is CollectionPageComponent.Intent.ClearFilter -> {
                _state.update {
                    it.copy(
                        currentParams = CollectionPageComponent.SelectedSortParams()
                    )
                }
                _fanficsList.setSection(SectionWithQuery(href = "collections/$relativeID"))
            }
            is CollectionPageComponent.Intent.Edit -> {
                navigation.activate(EditCollectionDialogConfig(realID))
            }
            CollectionPageComponent.Intent.Delete -> {
                coroutineScope.launch {
                    val result = collectionsRepo.delete(realID)
                    when(result) {
                        is ApiResult.Success -> {
                            runOnUiThread {
                                output(FanficsListComponent.Output.NavigateBack)
                            }
                        }
                        else -> {}
                    }
                }
            }
            is CollectionPageComponent.Intent.ChangeSubscription -> {
                coroutineScope.launch {
                    val result = collectionsRepo.follow(
                        follow = intent.follow,
                        collectionID = realID
                    )
                    when(result) {
                        is ApiResult.Error -> {}
                        is ApiResult.Success -> {
                            val currentPage = state.value.collectionPage
                            if(currentPage !is CollectionPageModelStable.Other) return@launch
                            val newPage = currentPage.copy(
                                subscribed = intent.follow
                            )
                            _state.update {
                                it.copy(collectionPage = newPage)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadPage() {
        coroutineScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            when(
                val result = collectionsRepo.getCollectionPage(relativeID)
            ) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = true,
                            errorMessage = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    println("success: ${result.value}")
                    _state.update {
                        it.copy(
                            loading = false,
                            collectionPage = result.value
                        )
                    }
                }
            }
        }
    }

    private fun buildSortParams(
        params: CollectionPageComponent.SelectedSortParams
    ): List<Pair<String, String>> {
        val list: MutableList<Pair<String, String>> = mutableListOf()

        list += Pair("search_string", params.searchText ?: "")
        list += Pair("fandom_id", params.fandom?.second ?: "")
        list += Pair("direction", params.direction?.second ?: "")
        list += Pair("sort", params.sort?.second ?: "6")
        return list
    }

    private fun getSavedSortParameter(): Pair<String, String>? {
        return sortTypeSetting?.let {
            val splitted = it.split('|')
            if (splitted.size == 2) {
                Pair(splitted[0], splitted[1])
            } else {
                null
            }
        }
    }

    private fun createSection(): SectionWithQuery {
        return SectionWithQuery(
            path = "collections/$relativeID",
            name = "",
            queryParameters = buildSortParams(
                state.value.currentParams
            )
        )
    }

    @Suppress("FoldInitializerAndIfToElvis")
    private fun onUpdateSuccess(
        state: EditCollectionComponent.State
    ) {
        navigation.dismiss()
        val currentPage = this.state.value.collectionPage
        if(currentPage == null) return
        val newPage = currentPage.copyPage(
            name = state.name,
            description = state.descriptor.takeIf(String::isNotEmpty)
        )
        _state.update {
            it.copy(collectionPage = newPage)
        }
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
        loadPage()
    }
}

class EditCollectionComponent(
    componentContext: ComponentContext,
    private val repository: ICollectionsRepo,
    private val coroutineScope: CoroutineScope,
    private val config: EditCollectionDialogConfig,
    private val onSuccess: (state: State) -> Unit,
    private val onCancel: () -> Unit
): ComponentContext by componentContext {
    private val _state = MutableValue(State())

    val state: Value<State> get() = _state

    fun onNameChange(newValue: String) {
        _state.update {
            it.copy(
                name = newValue.let { name ->
                    if(name.length > DESC_MAX_SIZE) name.substring(0, NAME_MAX_SIZE) else name
                }
            )
        }
    }

    fun onDescriptorChange(newValue: String) {
        _state.update {
            it.copy(
                descriptor = newValue.let { description ->
                    if(description.length > DESC_MAX_SIZE) description.substring(0, DESC_MAX_SIZE) else description
                }
            )
        }
    }

    fun onPublicChange(newValue: Boolean) {
        _state.update {
            it.copy(public = newValue)
        }
    }

    fun confirm() {
        coroutineScope.launch {
            val result = repository.update(
                config.collectionID,
                state.value.name,
                state.value.descriptor,
                state.value.public
            )
            when(result) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            error = true,
                            errorMessage = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> onSuccess(state.value)
            }
        }
    }

    fun cancel() = onCancel()

    private suspend fun loadInfo() {
        _state.update {
            it.copy(loading = true)
        }
        when(
            val result = repository.getMainInfo(config.collectionID)
        ) {
            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = true,
                        errorMessage = result.exception.message
                    )
                }
            }
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        loading = false,
                        name = result.value.name,
                        descriptor = result.value.description,
                        public = result.value.public
                    )
                }
            }
        }
    }

    init {
        coroutineScope.launch {
            loadInfo()
        }
    }

    data class State(
        val name: String = "",
        val descriptor: String = "",
        val public: Boolean = false,
        val loading: Boolean = false,
        val error: Boolean = false,
        val errorMessage: String? = null
    )

    companion object {
        private const val NAME_MAX_SIZE = 100
        private const val DESC_MAX_SIZE = 250
    }
}

@JvmInline
@Serializable
value class EditCollectionDialogConfig(val collectionID: String)