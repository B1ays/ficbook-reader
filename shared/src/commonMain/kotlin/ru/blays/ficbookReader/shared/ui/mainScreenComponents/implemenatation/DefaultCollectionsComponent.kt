package ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.CollectionsComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.CollectionsComponentInternal
import ru.blays.ficbookapi.data.CollectionsTypes
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultCollectionsComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    private val onOutput: (CollectionsComponent.Output) -> Unit
): CollectionsComponentInternal, ComponentContext by componentContext {
    private val _state: MutableValue<CollectionsComponent.State> = MutableValue(
        CollectionsComponent.State()
    )

    override fun refresh() {
        getCollections(collectionSection)
    }

    override val state: Value<CollectionsComponent.State>
        get() = _state

    private val collectionSection = CollectionsTypes._personalCollections

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        getCollections(collectionSection)
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    override fun sendIntent(intent: CollectionsComponent.Intent) {
        when(intent) {
            CollectionsComponent.Intent.Refresh -> refresh()
        }
    }

    override fun onOutput(output: CollectionsComponent.Output) {
        onOutput.invoke(output)
    }

    private fun getCollections(section: SectionWithQuery) {
        coroutineScope.launch {
            _state.update {
                it.copy(
                    isLoading = true
                )
            }
            val collections = ficbookApi
                .getCollections(section)
                .map { it.toStableModel() }
            _state.update {
                it.copy(
                    list = collections,
                    isLoading = false
                )
            }
        }
    }
}