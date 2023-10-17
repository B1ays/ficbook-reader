package ru.blays.ficbookReader.shared.ui.fanficListComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultFanficsListComponent(
    componentContext: ComponentContext,
    private val section: SectionWithQuery,
    private val ficbookApi: IFicbookApi,
    private val output: (output: FanficsListComponent.Output) -> Unit
): FanficsListComponent, ComponentContext by componentContext {
    private val _state: MutableValue<FanficsListComponent.State> = MutableValue(
            FanficsListComponent.State(
            section = section.toStableModel()
        )
    )
    override val state: Value<FanficsListComponent.State>
        get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        refresh()
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    override fun sendIntent(intent: FanficsListComponent.Intent) {
        when(intent) {
            is FanficsListComponent.Intent.Refresh -> refresh()
            is FanficsListComponent.Intent.LoadNextPage -> loadNextPage()
        }
    }

    override fun onOutput(output: FanficsListComponent.Output) {
        this.output.invoke(output)
    }

    private fun refresh() {
        coroutineScope.launch {
            _state.update {
                it.copy(
                    list = emptyList(),
                    isLoading = true
                )
            }
            val page = getPage(section, 1)
            _state.update {
                it.copy(
                    list = page,
                    isLoading = false,
                    page = 1
                )
            }
        }
    }

    private fun loadNextPage() {
        if(!state.value.isLoading) {
            coroutineScope.launch {
                _state.update {
                    it.copy(
                        isLoading = true
                    )
                }
                val nextPage = (state.value.page) + 1
                val page = getPage(section, nextPage)
                _state.update {
                    it.copy(
                        list = state.value.list + page,
                        isLoading = false,
                        page = nextPage
                    )
                }
            }
        }
    }

    private suspend fun getPage(section: SectionWithQuery, page: Int): List<FanficCardModelStable> {
        return ficbookApi.getFanficsForSection(section, page).map { it.toStableModel() }
    }
}