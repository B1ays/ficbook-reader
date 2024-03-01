package ru.blays.ficbook.reader.shared.ui.mainScreenComponents.implemenatation

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
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.data.mappers.toApiModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbook.reader.shared.data.sections.popularSections
import ru.blays.ficbook.reader.shared.data.sections.userSections
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbook.reader.shared.ui.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.ui.fanficListComponents.declaration.FanficsListComponentInternal
import ru.blays.ficbook.reader.shared.ui.fanficListComponents.implementation.DefaultFanficsListComponent
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.FeedComponent
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.FeedComponentInternal

class DefaultFeedComponent private constructor(
    componentContext: ComponentContext,
    fanficsList: (
        componentContext: ComponentContext,
        initialSection: SectionWithQuery,
        output: (FanficsListComponent.Output) -> Unit
    ) -> FanficsListComponentInternal,
    private val output: (FanficsListComponent.Output) -> Unit
): FeedComponentInternal, ComponentContext by componentContext {
    constructor(
        componentContext: ComponentContext,
        onOutput: (FanficsListComponent.Output) -> Unit
    ): this(
        componentContext = componentContext,
        fanficsList = { childContext, initialSection, output ->
            DefaultFanficsListComponent(
                componentContext = childContext,
                section = initialSection,
                output = output,
                loadAtCreate = false
            )
        },
        output = onOutput
    )

    private val authRepository: IAuthorizationRepo by getKoin().inject()

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
            if(authRepository.currentUserModel.value != null) {
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
                val section: SectionWithQuery? = Json.decodeFromString(rawJson)
                section?.let { _fanficListComponent.setSection(it) }
            }
        }
    }
}