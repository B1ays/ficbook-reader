package ru.blays.ficbook.reader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.data.CollectionsTypes
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICollectionsRepo
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.CollectionsComponent
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.CollectionsComponentInternal
class DefaultCollectionsComponent(
    componentContext: ComponentContext,
    private val onOutput: (CollectionsComponent.Output) -> Unit
): CollectionsComponentInternal, ComponentContext by componentContext {
    private val repository: ICollectionsRepo by getKoin().inject()

    private val _state: MutableValue<CollectionsComponent.State> = MutableValue(
        CollectionsComponent.State()
    )

    override val state get() = _state

    private val collectionSections = listOf(
        CollectionsTypes.personalCollections,
        CollectionsTypes.trackedCollections
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: CollectionsComponent.Intent) {
        when(intent) {
            CollectionsComponent.Intent.Refresh -> refresh()
        }
    }

    override fun onOutput(output: CollectionsComponent.Output) {
        onOutput.invoke(output)
    }

    override fun refresh() {
        getCollections(collectionSections)
    }

    private fun getCollections(
        sections: List<SectionWithQuery>
    ) {
        coroutineScope.launch {
            _state.update {
                it.copy(
                    list = emptyList(),
                    isLoading = true
                )
            }
            val results = sections.map { section ->
                repository.get(section, 1)
            }
            results.forEach { result ->
                when(result) {
                    is ApiResult.Success -> {
                        _state.update {
                            it.copy(
                                list = state.value.list + result.value.list
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _state.update {
                            it.copy(isError = true)
                        }
                    }
                }
            }
            _state.update {
                it.copy(isLoading = false)
            }
        }
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}