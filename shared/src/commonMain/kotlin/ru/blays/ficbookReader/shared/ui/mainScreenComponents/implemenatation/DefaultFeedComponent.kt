package ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import ru.blays.ficbookReader.shared.ui.fanficListComponents.DefaultFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.FeedComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.FeedComponentInternal
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultFeedComponent private constructor(
    componentContext: ComponentContext,
    initialSection: SectionWithQuery,
    fanficsList: (
        componentContext: ComponentContext,
        output: (FanficsListComponent.Output) -> Unit
    ) -> FanficsListComponent,
    private val output: (FanficsListComponent.Output) -> Unit
): FeedComponentInternal, ComponentContext by componentContext {
    constructor(
        componentContext: ComponentContext,
        initialSection: SectionWithQuery,
        ficbookApi: IFicbookApi,
        onOutput: (FanficsListComponent.Output) -> Unit
    ): this(
        componentContext = componentContext,
        initialSection = initialSection,
        fanficsList = { childContext, output ->
            DefaultFanficsListComponent(
                componentContext = childContext,
                section = initialSection,
                ficbookApi = ficbookApi,
                output = output
            )
        },
        output = onOutput
    )

    private val _state: MutableValue<SectionWithQuery> = MutableValue(initialSection)

    override val state: Value<SectionWithQuery> get() = _state

    override val fanficListComponent: FanficsListComponent = fanficsList(
        childContext(
            key = "fanficsList"
        ),
        output
    )

    override fun setState(value: SectionWithQuery) {
        _state.value = value
    }

    override fun refresh() {
        fanficListComponent.sendIntent(
            FanficsListComponent.Intent.Refresh
        )
    }

    override fun onIntent(intent: FeedComponent.Intent) {
        when(intent) {
            is FeedComponent.Intent.ChangeFeedSection -> {
                _state.update {
                    intent.section
                }
            }
        }
    }
}