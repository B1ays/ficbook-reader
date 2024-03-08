package ru.blays.ficbook.reader.shared.components.fanficListComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.FanficDirection
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficsListRepo
import ru.blays.ficbook.reader.shared.platformUtils.runOnUiThread
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficQuickActionsComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponentInternal
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.SnackbarHost
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.SnackbarMessageType

class DefaultFanficsListComponent(
    componentContext: ComponentContext,
    section: SectionWithQuery,
    loadAtCreate: Boolean = true,
    private val output: (output: FanficsListComponent.Output) -> Unit
): FanficsListComponentInternal, ComponentContext by componentContext {
    val repository: IFanficsListRepo by getKoin().inject()

    private val _quickActionComponents: MutableMap<String, FanficQuickActionsComponent> = mutableMapOf()

    private val _state: MutableValue<FanficsListComponent.State> = MutableValue(
        FanficsListComponent.State(
            section = section.toStableModel(),
            list = emptyList(),
            isLoading = false
        )
    )

    private val navigation = SlotNavigation<FanficsListComponent.DialogConfig>()

    override val dialog: Value<ChildSlot<*, FanficsListDialogComponent>> = childSlot(
        source = navigation,
        serializer = FanficsListComponent.DialogConfig.serializer(),

    ) { configuration, childContext ->
        FanficsListDialogComponent(
            componentContext = childContext,
            message = configuration.message,
            onOutput = navigation::dismiss
        )
    }

    override fun setSection(section: SectionWithQuery) {
        _state.update {
            it.copy(
                section = section.toStableModel()
            )
        }
        refresh()
    }

    override val state get() = _state

    private var hasNextPage: Boolean = true
    private var nextPage: Int = 1

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val settingsRepository: ISettingsRepository by inject(ISettingsRepository::class.java)

    private val superfilterSetting: String by settingsRepository.getDelegate(
        key = ISettingsRepository.stringKey(SettingsKeys.SUPERFILTER_KEY),
        defaultValue = ""
    )

    init {
        if(loadAtCreate) {
            refresh()
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

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
                val result = repository.get(
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
                        val deniedDirections = superfilterSetting
                            .removeSuffix(",")
                            .split(",")
                            .map(FanficDirection::getForName)

                        val fanfics = result.value.list
                        val hasNextPage = result.value.hasNextPage

                        this@DefaultFanficsListComponent.nextPage += 1
                        this@DefaultFanficsListComponent.hasNextPage = hasNextPage

                        val filteredFanfics = fanfics.filterNot {
                            it.status.direction in deniedDirections
                        }

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
}

class FanficsListDialogComponent(
    val componentContext: ComponentContext,
    private val message: String,
    private val onOutput: () -> Unit
): ComponentContext by componentContext {

}