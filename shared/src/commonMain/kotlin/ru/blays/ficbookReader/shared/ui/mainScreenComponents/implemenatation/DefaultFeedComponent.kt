package ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject
import ru.blays.ficbookReader.shared.data.mappers.toApiModel
import ru.blays.ficbookReader.shared.data.sections.popularSections
import ru.blays.ficbookReader.shared.data.sections.userSections
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbookReader.shared.ui.fanficListComponents.DefaultFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponentInternal
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.FeedComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.FeedComponentInternal
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultFeedComponent private constructor(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    fanficsList: (
        componentContext: ComponentContext,
        initialSection: SectionWithQuery,
        output: (FanficsListComponent.Output) -> Unit
    ) -> FanficsListComponentInternal,
    private val output: (FanficsListComponent.Output) -> Unit
): FeedComponentInternal, ComponentContext by componentContext {
    constructor(
        componentContext: ComponentContext,
        ficbookApi: IFicbookApi,
        onOutput: (FanficsListComponent.Output) -> Unit
    ): this(
        componentContext = componentContext,
        ficbookApi = ficbookApi,
        fanficsList = { childContext, initialSection, output ->
            DefaultFanficsListComponent(
                componentContext = childContext,
                section = initialSection,
                ficbookApi = ficbookApi,
                output = output
            )
        },
        output = onOutput
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val settingsComponent: ISettingsRepository by inject(ISettingsRepository::class.java)
    private var feedSettingsDelegate by settingsComponent.getDelegate(
        key = ISettingsRepository.stringKey(SettingsKeys.FEED_SECTION_KEY),
        defaultValue = Json.encodeToString(userSections.favourites.toApiModel())
    )
    private val feedSettingsFlow by settingsComponent.getFlowDelegate(
        key = ISettingsRepository.stringKey(SettingsKeys.FEED_SECTION_KEY),
        defaultValue = feedSettingsDelegate
    )


    private val _fanficListComponent: FanficsListComponentInternal = fanficsList(
        childContext("fanficsList"),
        getFeedSection(),
        output
    )

    override val fanficListComponent: FanficsListComponent = _fanficListComponent

    private fun getFeedSection(): SectionWithQuery {
        val saved = feedSettingsDelegate
        return try {
            Json.decodeFromString(saved)
        } catch (e: Exception) {
            e.printStackTrace()
            if(ficbookApi.isAuthorized.value) {
                userSections.favourites.toApiModel()
            } else {
                popularSections.allPopular.toApiModel()
            }
        }
    }

    override fun refresh() {
        fanficListComponent.sendIntent(
            FanficsListComponent.Intent.Refresh
        )
    }

    override fun onIntent(intent: FeedComponent.Intent) {
        when(intent) {
            is FeedComponent.Intent.SetFeedSection -> {
                _fanficListComponent.setSection(intent.section)
                feedSettingsDelegate = Json.encodeToString(intent.section)
            }
        }
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
        coroutineScope.launch {
            feedSettingsFlow.collect { rawJson ->
                //println("New section from flow: $rawJson")
                val section: SectionWithQuery? = Json.decodeFromString(rawJson)
                section?.let { _fanficListComponent.setSection(it) }
            }
        }
    }
}