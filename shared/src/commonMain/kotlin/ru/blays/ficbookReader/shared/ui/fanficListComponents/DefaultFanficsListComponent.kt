package ru.blays.ficbookReader.shared.ui.fanficListComponents

import com.arkivanov.decompose.ComponentContext
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
import ru.blays.ficbookReader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbookReader.shared.data.dto.FanficDirection
import ru.blays.ficbookReader.shared.data.mappers.toApiModel
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.repo.declaration.IFanficsListRepo
import ru.blays.ficbookReader.shared.platformUtils.runOnUiThread
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.result.ApiResult

class DefaultFanficsListComponent(
    componentContext: ComponentContext,
    section: SectionWithQuery,
    loadAtCreate: Boolean = true,
    private val output: (output: FanficsListComponent.Output) -> Unit
): FanficsListComponentInternal, ComponentContext by componentContext {
    val repository: IFanficsListRepo by getKoin().inject()

    private val _state: MutableValue<FanficsListComponent.State> = MutableValue(
            FanficsListComponent.State(
            section = section.toStableModel()
        )
    )

    private val navigation = SlotNavigation<FanficsListComponent.DialogConfig>()

    override val dialog: Value<ChildSlot<*, FanficsListDialogComponent>> = childSlot(
        source = navigation,
        serializer = FanficsListComponent.DialogConfig.serializer(),

    ) { configuration, childContext ->
        FanficsListDialogComponent(
            componentContext = childContext,
            message = configuration.message
        ) {
            navigation.dismiss()
        }
    }

    override fun setSection(section: SectionWithQuery) {
        _state.update {
            it.copy(
                section = section.toStableModel()
            )
        }
        refresh()
    }

    override val state: Value<FanficsListComponent.State>
        get() = _state

    private var hasNextPage: Boolean = true

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

    private fun refresh() {
        coroutineScope.launch {
            _state.update {
                it.copy(
                    list = emptyList(),
                    isLoading = true
                )
            }
            val page = getPage(
                section = state.value.section.toApiModel(),
                page = 1
            )
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
        if(!state.value.isLoading && hasNextPage) {
            coroutineScope.launch {
                _state.update {
                    it.copy(
                        isLoading = true
                    )
                }
                val nextPage = (state.value.page) + 1
                val page = getPage(
                    section = state.value.section.toApiModel(),
                    page = nextPage
                )
                _state.update {
                    it.copy(
                        list = it.list + page,
                        isLoading = false,
                        page = nextPage
                    )
                }
            }
        }
    }

    private suspend fun getPage(
        section: SectionWithQuery,
        page: Int
    ): List<FanficCardModelStable> {
        val result = repository.get(
            section = section,
            page = page
        )
        return when (result) {
            is ApiResult.Error -> {
                runOnUiThread {
                    navigation.activate(
                        FanficsListComponent.DialogConfig(result.exception.message ?: "Unknown error")
                    )
                }
                return emptyList()
            }
            is ApiResult.Success -> {
                val deniedDirections = superfilterSetting
                    .removeSuffix(",")
                    .split(",")
                    .map(FanficDirection::getForName)

                val (fanfics, hasNextPage) = result.value

                this.hasNextPage = hasNextPage

                fanfics.filterNot {
                    it.status.direction in deniedDirections
                }
            }
        }
    }

    private fun setAsFeed(section: ru.blays.ficbookReader.shared.data.dto.SectionWithQuery) {
        settingsRepository.setValueForKey(
            key = ISettingsRepository.stringKey(SettingsKeys.FEED_SECTION_KEY),
            value = Json.encodeToString(section)
        )
    }
}

class FanficsListDialogComponent(
    val componentContext: ComponentContext,
    private val message: String,
    private val onOutput: () -> Unit
): ComponentContext by componentContext {

}