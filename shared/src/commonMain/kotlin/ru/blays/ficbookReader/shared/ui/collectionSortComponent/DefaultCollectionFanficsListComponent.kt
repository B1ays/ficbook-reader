package ru.blays.ficbookReader.shared.ui.collectionSortComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.russhwolf.settings.nullableString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.repo.declaration.ICollectionsRepo
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.settings
import ru.blays.ficbookReader.shared.ui.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.declaration.FanficsListComponentInternal
import ru.blays.ficbookReader.shared.ui.fanficListComponents.implementation.DefaultFanficsListComponent
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.result.ApiResult

class DefaultCollectionFanficsListComponent(
    componentContext: ComponentContext,
    private val initialSection: SectionWithQuery,
    output: (output: FanficsListComponent.Output) -> Unit
): CollectionFanficsListComponent, ComponentContext by componentContext {
    private val collectionsRepo: ICollectionsRepo by getKoin().inject()

    private var sortTypeSetting by settings.nullableString(
        key = SettingsKeys.COLLECTION_SORT_TYPE_KEY
    )

    private val _state = MutableValue(
        CollectionFanficsListComponent.State(
            collectionName = initialSection.name,
            availableParams = null,
            currentParams = CollectionFanficsListComponent.SelectedSortParams(
                sort = getSavedSortParameter()
            ),
            loading = true,
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

    override val state get() = _state
    override val fanficsListComponent: FanficsListComponent
        get() = _fanficsList

    private var collectionID = initialSection.path.substringAfterLast('/')

    override fun onIntent(intent: CollectionFanficsListComponent.Intent) {
        when(intent) {
            is CollectionFanficsListComponent.Intent.ChangeDirection -> {
                _state.update {
                    it.copy(
                        currentParams = it.currentParams.copy(
                            direction = intent.directionCode
                        )
                    )
                }
            }
            is CollectionFanficsListComponent.Intent.ChangeFandom -> {
                _state.update {
                    it.copy(
                        currentParams = it.currentParams.copy(
                            fandom = intent.fandomCode
                        )
                    )
                }
            }
            is CollectionFanficsListComponent.Intent.ChangeSearchText -> {
                _state.update {
                    it.copy(
                        currentParams = it.currentParams.copy(
                            searchText = intent.searchText
                        )
                    )
                }
            }
            is CollectionFanficsListComponent.Intent.ChangeSortType -> {
                _state.update {
                    it.copy(
                        currentParams = it.currentParams.copy(
                            sort = intent.sortTypeCode
                        )
                    )
                }
                sortTypeSetting = intent.sortTypeCode.let { "${it.first}|${it.second}" }
            }
            is CollectionFanficsListComponent.Intent.Search -> {
                _fanficsList.setSection(
                    section = createSection()
                )
            }
            is CollectionFanficsListComponent.Intent.Refresh -> {
                getSortParams()
            }

            CollectionFanficsListComponent.Intent.Clear -> {
                _state.update {
                    it.copy(
                        currentParams = CollectionFanficsListComponent.SelectedSortParams()
                    )
                }
                _fanficsList.setSection(initialSection)
            }
        }
    }

    private fun getSortParams() {
        coroutineScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            when(
                val result = collectionsRepo.getCollectionSortParams(collectionID)
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
                            availableParams = result.value,
                        )
                    }
                }
            }
        }
    }

    private fun buildSortParams(
        params: CollectionFanficsListComponent.SelectedSortParams
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
            path = initialSection.path,
            name = initialSection.name,
            queryParameters = buildSortParams(
                _state.value.currentParams
            )
        )
    }

    init {
        getSortParams()
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}