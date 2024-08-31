package ru.blays.ficbook.reader.shared.components.fanficListComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.saveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficQuickActionsComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponentInternal
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.SnackbarHost
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.SnackbarMessageType
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficsListRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo
import ru.blays.ficbook.reader.shared.platformUtils.runOnUiThread
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue

@OptIn(ExperimentalStateKeeperApi::class)
class DefaultFanficsListComponent(
    componentContext: ComponentContext,
    section: SectionWithQuery,
    loadAtCreate: Boolean = true,
    private val output: (output: FanficsListComponent.Output) -> Unit
): FanficsListComponentInternal, ComponentContext by componentContext, KoinComponent {
    private val fanficsRepository: IFanficsListRepo by inject()
    private val filtersRepository: IFiltersRepo by inject()
    private val settingsRepository: ISettingsRepository by inject()

    private val filter = filtersRepository.getFilter()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _quickActionComponents: MutableMap<String, FanficQuickActionsComponent> = mutableMapOf()

    private val _state = SaveableMutableValue(
        serializer = FanficsListComponent.State.serializer(),
        initialValue = FanficsListComponent.State(
            section = section.toStableModel(),
            list = emptyList(),
            isLoading = false
        )
    )

    private val navigation = SlotNavigation<FanficsListComponent.DialogConfig>()

    override fun setSection(section: SectionWithQuery) {
        _state.update {
            it.copy(
                section = section.toStableModel()
            )
        }
        refresh()
    }

    override val state get() = _state

    private var hasNextPage: Boolean by saveable(
        serializer = Boolean.serializer(),
        key = HAS_NEXT_PAGE_KEY,
        init = { true }
    )
    private var nextPage: Int by saveable(
        serializer = Int.serializer(),
        key = NEXT_PAGE_KEY,
        init = { 1 }
    )

    override fun sendIntent(intent: FanficsListComponent.Intent) {
        when(intent) {
            is FanficsListComponent.Intent.Refresh -> refresh()
            is FanficsListComponent.Intent.LoadNextPage -> loadNextPage()
            is FanficsListComponent.Intent.SetAsFeed -> setAsFeed(intent.section)
        }
    }

    override fun onOutput(output: FanficsListComponent.Output) {
        this.output.invoke(output)
    }

    override fun getQuickActionsComponent(fanficID: String): FanficQuickActionsComponent {
        return _quickActionComponents.getOrPut(key = fanficID) {
            println("New component created for $fanficID")
            DefaultFanficQuickActionsComponent(
                componentContext = childContext(key = "QuickActions-$fanficID"),
                fanficID = fanficID
            )
        }
    }

    private fun refresh() {
        coroutineScope.launch {
            nextPage = 1
            hasNextPage = true
            _state.update {
                it.copy(list = emptyList())
            }
            loadNextPage()
        }
    }

    private fun loadNextPage() {
        if(!state.value.isLoading && hasNextPage) {
            coroutineScope.launch {
                _state.update {
                    it.copy(isLoading = true)
                }
                val result = fanficsRepository.get(
                    section = state.value.section,
                    page = nextPage
                )
                when (result) {
                    is ApiResult.Error -> {
                        runOnUiThread {
                            navigation.activate(
                                FanficsListComponent.DialogConfig(result.exception.message ?: "Unknown error")
                            )
                        }
                        _state.update {
                            it.copy(isLoading = false)
                        }
                    }
                    is ApiResult.Success -> {
                        nextPage++
                        hasNextPage = result.value.hasNextPage

                        val filteredFanfics = result.value.list.filter(filter::filter)

                        _state.update {
                            it.copy(
                                list = it.list + filteredFanfics,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setAsFeed(section: ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery) {
        settingsRepository.setValueForKey(
            key = ISettingsRepository.stringKey(SettingsKeys.FEED_SECTION_KEY),
            value = Json.encodeToString(section)
        )
        coroutineScope.launch {
            SnackbarHost.showMessage(
                message = "Секция ${section.name} установлена как лента",
                infoType = SnackbarMessageType.INFO
            )
        }
    }

    init {
        lifecycle.doOnStart(true) {
            val state = state.value
            if(state.list.isEmpty() && loadAtCreate) {
                refresh()
            }
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    companion object {
        private const val HAS_NEXT_PAGE_KEY = "has_next_page"
        private const val NEXT_PAGE_KEY = "next_page"
    }
}