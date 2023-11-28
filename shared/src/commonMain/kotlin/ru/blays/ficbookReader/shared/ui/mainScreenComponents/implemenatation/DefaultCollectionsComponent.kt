package ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.repo.declaration.ICollectionsRepo
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.CollectionsComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.CollectionsComponentInternal
import ru.blays.ficbookapi.data.CollectionsTypes
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.result.ApiResult

class DefaultCollectionsComponent(
    componentContext: ComponentContext,
    private val onOutput: (CollectionsComponent.Output) -> Unit
): CollectionsComponentInternal, ComponentContext by componentContext {
    private val repository: ICollectionsRepo by getKoin().inject()

    private val _state: MutableValue<CollectionsComponent.State> = MutableValue(
        CollectionsComponent.State()
    )

    override val state get() = _state

    private val collectionSection = CollectionsTypes._personalCollections

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
        getCollections(collectionSection)
    }

    private fun getCollections(section: SectionWithQuery) {
        coroutineScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            when(
                val result = repository.get(section, 1)
            ) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            list = result.value.list,
                            isLoading = false
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
    }

    init {
        getCollections(collectionSection)
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}