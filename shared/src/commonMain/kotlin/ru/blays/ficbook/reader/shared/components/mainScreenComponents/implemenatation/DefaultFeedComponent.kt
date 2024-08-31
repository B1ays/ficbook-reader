package ru.blays.ficbook.reader.shared.components.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.nullableString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponentInternal
import ru.blays.ficbook.reader.shared.components.fanficListComponents.implementation.DefaultFanficsListComponent
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration.FeedComponent
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration.FeedComponentInternal
import ru.blays.ficbook.reader.shared.data.mappers.toApiModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbook.reader.shared.data.sections.UserSectionsStable
import ru.blays.ficbook.reader.shared.data.sections.popularSections
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.settings

@OptIn(ExperimentalSettingsApi::class)
class DefaultFeedComponent private constructor(
    componentContext: ComponentContext,
    fanficsList: (
        componentContext: ComponentContext,
        initialSection: SectionWithQuery,
        output: (FanficsListComponent.Output) -> Unit
    ) -> FanficsListComponentInternal,
    private val output: (FanficsListComponent.Output) -> Unit
): FeedComponentInternal, ComponentContext by componentContext, KoinComponent {
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

    private val authRepository: IAuthorizationRepo by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var feedSetting by settings.nullableString(
        key = SettingsKeys.FEED_SECTION_KEY
    )
    private val feedSettingsFlow = settings.getStringOrNullFlow(
        key = SettingsKeys.FEED_SECTION_KEY
    )

    private val _fanficListComponent: FanficsListComponentInternal = fanficsList(
        childContext("fanficsList"),
        getFeedSection(),
        output
    )

    override val fanficListComponent: FanficsListComponent = _fanficListComponent

    private fun getFeedSection(): SectionWithQuery {
        val saved = feedSetting
        if(saved != null) {
            try {
                return Json.decodeFromString(saved)
            } catch (_: Exception) {}
        }
        return if(authRepository.hasSavedAccount && !authRepository.anonymousMode) {
            UserSectionsStable.default.favourites.toApiModel()
        } else {
            popularSections.allPopular.toApiModel()
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
                feedSetting = Json.encodeToString(intent.section)
            }
        }
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
        coroutineScope.launch {
            feedSettingsFlow.collect { json ->
                if(json == null) return@collect
                val section: SectionWithQuery = Json.decodeFromString<SectionWithQuery?>(json) ?: return@collect
                _fanficListComponent.setSection(section)
            }
        }
    }
}