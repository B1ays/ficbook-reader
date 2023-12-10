package ru.blays.ficbookReader.shared.ui.searchComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import ru.blays.ficbookReader.shared.data.dto.IntRangeSimple
import ru.blays.ficbookReader.shared.data.dto.SearchParams
import ru.blays.ficbookReader.shared.ui.fanficListComponents.DefaultFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchComponent
import ru.blays.ficbookapi.SEARCH_HREF
import ru.blays.ficbookapi.data.SectionWithQuery

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

    private val _state = MutableValue(SearchParams.default)

    override val state get() = _state

    override val fanficsListComponent = _fanficsListComponent
    override val searchFandomsComponent = DefaultSearchFandomsComponent(
        componentContext = childContext("SearchFandomsComponent")
    )

    override val searchTagsComponent = DefaultSearchTagsComponent(
        componentContext = childContext("SearchTagsComponent")
    )

    override fun search() {
        _fanficsListComponent.setSection(
            section = buildSection()
        )
    }

    override fun clear() {
        _state.update { SearchParams.default }
    }

    override fun setFandomsFilter(value: String) {
        _state.update { it.copy(fandomsFilter = value) }
    }

    override fun setFandomsGroup(value: Int) {
        _state.update { it.copy(fandomsGroup = value) }
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

    override fun setTranslate(value: Int) {
        _state.update {
            it.copy(translate = value)
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

    fun getSection(): SectionWithQuery = buildSection() //TODO Remove after all tests

    private fun buildSection(): SectionWithQuery {
        val queryParams = mutableListOf<Pair<String, String>>()

        _state.value.run {
            queryParams.add("fandom_filter" to fandomsFilter)

            val fandomsState = searchFandomsComponent.state.value
            when(fandomsFilter) {
                SearchParams.FANDOM_FILTER_CATEGORY -> {
                    queryParams.add("fandom_group_id" to "$fandomsGroup")
                    fandomsState.excludedFandoms.forEach { fandom ->
                        queryParams.add("fandom_exclude_ids[]" to fandom.id)
                    }
                }
                SearchParams.FANDOM_FILTER_CONCRETE -> {
                    fandomsState.selectedFandoms.forEach { fandom ->
                        queryParams.add("fandom_ids[]" to fandom.id)
                    }
                    fandomsState.excludedFandoms.forEach { fandom ->
                        queryParams.add("fandom_exclude_ids[]" to fandom.id)
                    }
                }
            }

            queryParams.add("pages_min" to "${pagesCountRange.start.let { if(it == 0) "" else it }}")
            queryParams.add("pages_max" to "${pagesCountRange.end.let { if(it == 0) "" else it }}")

            queryParams.add("transl" to "$translate")

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

            queryParams.add("likes_min" to "${likesRange.start.let { if(it == 0) "" else it }}")
            queryParams.add("likes_max" to "${likesRange.end.let { if(it == 0) "" else it }}")

            queryParams.add("rewards_min" to "$minRewards")

            queryParams.add("title" to title)

            queryParams.add("filter_readed" to if(filterReaded) "1" else "0")

            queryParams.add("sort" to "$sort")

            queryParams.add("find" to "Найти!")
        }

        val section = SectionWithQuery(
            name = "Поиск",
            queryParameters = queryParams,
            path = SEARCH_HREF
        )
        return section
    }
}