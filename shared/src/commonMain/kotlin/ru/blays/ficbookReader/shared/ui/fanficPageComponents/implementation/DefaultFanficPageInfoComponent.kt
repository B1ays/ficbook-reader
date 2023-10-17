package ru.blays.ficbookReader.shared.ui.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageActionsComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultFanficPageInfoComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    private val fanficHref: String,
    private val onOutput: (FanficPageInfoComponent.Output) -> Unit
): FanficPageInfoComponent, ComponentContext by componentContext {
    private val _state = MutableValue(FanficPageInfoComponent.State())
    override val state: Value<FanficPageInfoComponent.State>
        get() = _state

    override val actionsComponent: FanficPageActionsComponent = DefaultFanficPageActionsComponent(
        componentContext = childContext(
            key = "actions"
        ),
        ficbookApi = ficbookApi,
        fanficID = fanficHref
            .split("/")
            .lastOrNull()
            ?: ""
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        loadPage()
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    override fun sendIntent(intent: FanficPageInfoComponent.Intent) {
        when(intent) {
            is FanficPageInfoComponent.Intent.Refresh -> loadPage()
        }
    }

    override fun onOutput(output: FanficPageInfoComponent.Output) {
        onOutput.invoke(output)
    }

    private fun loadPage() {
        coroutineScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            val page = ficbookApi.getFanficPageByHref(fanficHref)
            _state.update {
                it.copy(
                    fanfic = page?.toStableModel(),
                    isLoading = false
                )
            }
        }
    }
}