package ru.blays.ficbook.reader.shared.components.searchComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnStart
import ru.blays.ficbook.api.SEARCH_HREF
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.implementation.DefaultFanficsListComponent
import ru.blays.ficbook.reader.shared.components.searchComponents.declaration.SearchComponent
import ru.blays.ficbook.reader.shared.data.IntRangeSimple
import ru.blays.ficbook.reader.shared.data.SearchParams
import ru.blays.ficbook.reader.shared.data.SearchedFandomModel
import ru.blays.ficbook.reader.shared.data.realm.entity.*
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue

class DefaultSearchComponent(
    componentContext: ComponentContext,
    output: (output: FanficsListComponent.Output) -> Unit
): SearchComponent, ComponentContext by componentContext {
    private val _fanficsListComponent = DefaultFanficsListComponent(
        componentContext = childContext("FanficsListComponent"),
        section = SectionWithQuery(""),
        loadAtCreate = false,
        output = output
    )
    private val _searchFandomsComponent = DefaultSearchFandomsComponent(
    componentContext = childContext("SearchFandomsComponent")
    )
    private val _searchPairingsComponent = DefaultSearchPairingsComponent(
        componentContext = childContext("SearchPairingsComponent")
    )
    private val _searchTagsComponent = DefaultSearchTagsComponent(
        componentContext = childContext("SearchTagsComponent")
    )

    private val _state = SaveableMutableValue(
        serializer = SearchParams.serializer(),
        initialValue = SearchParams.Default
    )

    override val state get() = _state

    override val fanficsListComponent get() = _fanficsListComponent
    override val searchFandomsComponent get() = _searchFandomsComponent
    override val searchCharactersComponent get() = _searchPairingsComponent
    override val searchTagsComponent get() = _searchTagsComponent
    override val savedSearchesComponent = DefaultSearchSaveComponent(
        componentContext = childContext("SavedSearchesComponent"),
        onSelect = ::onSavedSearchSelected,
        createEntity = ::createSearchParamsEntity
    )

    override fun search() {
        _fanficsListComponent.setSection(
            section = buildSection()
        )
    }

    override fun clear() {
        _state.update { SearchParams.Default }
    }

    override fun setSearchOriginals(value: Boolean) {
        _state.update {
            it.copy(searchOriginals = value)
        }
    }

    override fun setSearchFanfics(value: Boolean) {
        _state.update {
            it.copy(searchFanfics = value)
        }
    }

    override fun setPagesCountRange(value: IntRangeSimple) {
        _state.update {
            it.copy(pagesCountRange = value)
        }
    }

    override fun setStatus(value: List<Int>) {
        _state.update {
            it.copy(withStatus = value)
        }
    }

    override fun setRating(value: List<Int>) {
        _state.update {
            it.copy(withRating = value)
        }
    }

    override fun setDirection(value: List<Int>) {
        _state.update {
            it.copy(withDirection = value)
        }
    }

    override fun setOnlyTranslations(value: Boolean) {
        _state.update {
            it.copy(onlyTranslations = value)
        }
    }

    override fun setOnlyPremium(value: Boolean) {
        _state.update {
            it.copy(onlyPremium = value)
        }
    }

    override fun setLikesRange(value: IntRangeSimple) {
        _state.update {
            it.copy(likesRange = value)
        }
    }

    override fun setMinRewards(value: Int) {
        _state.update {
            it.copy(minRewards = value)
        }
    }

    override fun setMinComments(value: Int) {
        _state.update {
            it.copy(minComments = value)
        }
    }

    override fun setDateRange(value: LongRange) {
        _state.update {
            it.copy(dateRange = value)
        }
    }

    override fun setTitle(value: String) {
        _state.update {
            it.copy(title = value)
        }
    }

    override fun setFilterReaded(value: Boolean) {
        _state.update {
            it.copy(filterReaded = value)
        }
    }

    override fun setSort(value: Int) {
        _state.update {
            it.copy(sort = value)
        }
    }

    private fun buildSection(): SectionWithQuery {
        val queryParams = mutableListOf<Pair<String, String>>()

        state.value.run {
            if(searchOriginals) {
                queryParams.add("work_types[]" to "originals")
            }
            if(searchFanfics) {
                queryParams.add("work_types[]" to "fandom")

                val fandomsState = searchFandomsComponent.state.value

                if(fandomsState.selectedFandoms.isNotEmpty()) {
                    fandomsState.selectedFandoms.forEach { fandom ->
                        queryParams.add("fandom_ids[]" to fandom.id)
                    }

                    val pairingsState = _searchPairingsComponent.state.value
                    pairingsState.selectedPairings.forEachIndexed { index, pairing ->
                        pairing.characters.forEach { character ->
                            queryParams.add(
                                "pairings[$index][chars][]" to character.id
                            )
                        }
                        if(pairing.characters.isNotEmpty()) {
                            queryParams.add(
                                "pairings[$index][pairing]" to pairing.characters.joinToString("---") {
                                    "${if(it.modifier.isNotEmpty()) "${it.modifier}!" else ""}${it.name}"
                                }
                            )
                        }

                    }
                    pairingsState.excludedPairings.forEachIndexed { index, pairing ->
                        pairing.characters.forEach { character ->
                            queryParams.add(
                                "pairings_exclude[$index][chars][]" to character.id
                            )
                        }
                        if(pairing.characters.isNotEmpty()) {
                            queryParams.add(
                                "pairings_exclude[$index][pairing]" to pairing.characters.joinToString("---") {
                                    "${if(it.modifier.isNotEmpty()) "${it.modifier}!" else ""}${it.name}"
                                }
                            )
                        }
                    }
                }
                if(fandomsState.excludedFandoms.isNotEmpty()) {
                    fandomsState.excludedFandoms.forEach { fandom ->
                        queryParams.add("fandom_exclude_ids[]" to fandom.id)
                    }
                    queryParams.add("fandoms_search_exclude_options" to "2")
                } else {
                    queryParams.add("fandoms_search_exclude_options" to "1")
                }
                queryParams.add("fandoms_search_options" to "2")

            }

            queryParams.add("pages_min" to pagesCountRange.start.takeIf { it != 0 }?.toString().orEmpty())
            queryParams.add("pages_max" to pagesCountRange.end.takeIf { it != 0 }?.toString().orEmpty())

            if(onlyTranslations) {
                queryParams.add("translations_only" to "1")
            }

            withStatus.forEach { status ->
                queryParams.add("statuses[]" to "$status")
            }

            withRating.forEach { rating ->
                queryParams.add("ratings[]" to "$rating")
            }

            withDirection.forEach { direction ->
                queryParams.add("directions[]" to "$direction")
            }

            queryParams.add("only_premium" to if(onlyPremium) "1" else "0")

            val tagsState = searchTagsComponent.state.value
            tagsState.selectedTags.forEach { tag ->
                queryParams.add("tags_include[]" to tag.id)
            }
            tagsState.excludedTags.forEach { tag ->
                queryParams.add("tags_exclude[]" to tag.id)
            }
            queryParams.add("tags_search_options" to "${tagsState.behavior}")

            queryParams.add("likes_min" to likesRange.start.takeIf { it != 0 }?.toString().orEmpty())
            queryParams.add("likes_max" to likesRange.end.takeIf { it != 0 }?.toString().orEmpty())

            queryParams.add("rewards_min" to "$minRewards")
            queryParams.add("comments_min" to "$minComments")

            queryParams.add("title" to title)

            queryParams.add("include_unread" to if(filterReaded) "1" else "0")

            queryParams.add("sort" to "$sort")

            queryParams.add("find" to "#result")
        }

        val section = SectionWithQuery(
            name = "Поиск",
            queryParameters = queryParams,
            path = SEARCH_HREF
        )

        return section
    }

    private fun onSavedSearchSelected(searchParamsEntity: SearchParamsEntity) {
        val searchParams = SearchParams(
            searchOriginals = searchParamsEntity.searchOriginals,
            searchFanfics = searchParamsEntity.searchFanfics,
            pagesCountRange = searchParamsEntity.pagesCountRange?.let {
                IntRangeSimple(it.start , it.end)
            } ?: IntRangeSimple.EMPTY,
            withStatus = searchParamsEntity.withStatus,
            withRating = searchParamsEntity.withRating,
            withDirection = searchParamsEntity.withDirection,
            onlyTranslations = searchParamsEntity.onlyTranslations,
            onlyPremium = searchParamsEntity.onlyPremium,
            likesRange = searchParamsEntity.likesRange?.let {
                IntRangeSimple(it.start, it.end)
            } ?: IntRangeSimple.EMPTY,
            minRewards = searchParamsEntity.minRewards,
            minComments = searchParamsEntity.minComments,
            dateRange = searchParamsEntity.dateRange?.let {
                it.start..it.end
            } ?: LongRange.EMPTY,
            title = searchParamsEntity.title,
            filterReaded = searchParamsEntity.filterReaded,
            sort = searchParamsEntity.sort,
        )
        _state.value = searchParams
        _searchFandomsComponent.updateState {
            it.copy(
                selectedFandoms = searchParamsEntity.includedFandoms.mapTo(mutableSetOf(), FandomEntity::toDtoModel),
                excludedFandoms = searchParamsEntity.excludedFandoms.mapTo(mutableSetOf(), FandomEntity::toDtoModel)
            )
        }
        _searchPairingsComponent.updateState {
            it.copy(
                selectedPairings = searchParamsEntity.includedPairings.mapTo(mutableSetOf(), PairingEntity::toDtoModel),
                excludedPairings = searchParamsEntity.excludedPairings.mapTo(mutableSetOf(), PairingEntity::toDtoModel)
            )
        }
        _searchTagsComponent.updateState {
            it.copy(
                selectedTags = searchParamsEntity.includedTags.mapTo(mutableSetOf(), TagEntity::toDtoModel),
                excludedTags = searchParamsEntity.excludedTags.mapTo(mutableSetOf(), TagEntity::toDtoModel)
            )
        }
    }

    private fun createSearchParamsEntity(
        name: String,
        description: String
    ): SearchParamsEntity {
        return SearchParamsEntity(
            name = name,
            description = description,
            searchParams = state.value,
            includedFandoms = searchFandomsComponent.state.value.selectedFandoms,
            excludedFandoms = searchFandomsComponent.state.value.excludedFandoms,
            includedPairings = searchCharactersComponent.state.value.selectedPairings,
            excludedPairings = searchCharactersComponent.state.value.excludedPairings,
            includedTags = searchTagsComponent.state.value.selectedTags,
            excludedTags = searchTagsComponent.state.value.excludedTags
        )
    }

    private fun observeFandomsChange() {
        var previousIds: List<String> = emptyList()
        searchFandomsComponent.state.subscribe { state ->
            if(state.selectedFandoms.isNotEmpty()) {
                val fandomsIds = state.selectedFandoms.map(SearchedFandomModel::id)
                if(fandomsIds != previousIds) {
                    _searchPairingsComponent.excludeNotLinkedPairings(fandomsIds)
                    _searchPairingsComponent.update(fandomsIds)
                }
                previousIds = fandomsIds
            } else {
                _searchPairingsComponent.clean()
                previousIds = emptyList()
            }
        }
    }

    init {
        lifecycle.doOnStart(true) {
            observeFandomsChange()
        }
    }
}