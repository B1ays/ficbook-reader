package ru.blays.ficbook.reader.shared.components.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.data.IntRangeSimple
import ru.blays.ficbook.reader.shared.data.SearchParams

interface SearchComponent {
    val state: Value<SearchParams>

    val fanficsListComponent: FanficsListComponent
    val searchFandomsComponent: SearchFandomsComponent
    val searchTagsComponent: SearchTagsComponent
    val searchCharactersComponent: SearchPairingsComponent
    val savedSearchesComponent: SearchSaveComponent

    fun search()
    fun clear()

    /**
    * Fun's for change search params
    **/
    fun setFandomsFilter(value: String)
    fun setFandomsGroup(value: Int)
    fun setPagesCountRange(value: IntRangeSimple)
    fun setStatus(value: List<Int>)
    fun setRating(value: List<Int>)
    fun setDirection(value: List<Int>)
    fun setTranslate(value: Int)
    fun setOnlyPremium(value: Boolean)
    fun setLikesRange(value: IntRangeSimple)
    fun setMinRewards(value: Int)
    fun setMinComments(value: Int)
    fun setDateRange(value: LongRange)
    fun setTitle(value: String)
    fun setFilterReaded(value: Boolean)
    fun setSort(value: Int)

    sealed class Output {
        data object NavigateBack: Output()
    }
}