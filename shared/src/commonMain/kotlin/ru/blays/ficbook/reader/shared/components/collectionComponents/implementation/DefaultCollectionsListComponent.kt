package ru.blays.ficbook.reader.shared.components.collectionComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponent
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponentInternal
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICollectionsRepo

class DefaultCollectionsListComponent(
    componentContext: ComponentContext,
    private val sections: Array<SectionWithQuery>,
    private val onOutput: (CollectionsListComponent.Output) -> Unit
): CollectionsListComponentInternal, ComponentContext by componentContext {
    private val collectionsRepo: ICollectionsRepo by getKoin().inject()

    private val _state: MutableValue<CollectionsListComponent.State> = MutableValue(
        CollectionsListComponent.State()
    )

    override val state get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: CollectionsListComponent.Intent) {
        when(intent) {
            is CollectionsListComponent.Intent.Refresh -> refresh()
            is CollectionsListComponent.Intent.CreateCollection -> {
                coroutineScope.launch {
                    val result = collectionsRepo.create(
                        name = intent.name,
                        description = intent.description,
                        public = intent.public
                    )
                    when(result) {
                        is ApiResult.Error -> {}
                        is ApiResult.Success -> refresh()
                    }
                }
            }
            is CollectionsListComponent.Intent.UpdateCollection -> {
                onOutput(
                    CollectionsListComponent.Output.OpenCollection(
                        relativeID = intent.relativeID,
                        realID = intent.realID,
                        initialDialogConfig = EditCollectionDialogConfig(intent.realID)
                    )
                )
            }
            is CollectionsListComponent.Intent.DeleteCollection -> {
                coroutineScope.launch {
                    val result = collectionsRepo.delete(intent.realID)
                    when(result) {
                        is ApiResult.Error -> {}
                        is ApiResult.Success -> refresh()
                    }
                }
            }
            is CollectionsListComponent.Intent.ChangeSubscription -> {
                coroutineScope.launch {
                    val result = collectionsRepo.follow(
                        follow = !intent.collection.subscribed,
                        collectionID = intent.collection.realID
                    )
                    when(result) {
                        is ApiResult.Error -> {}
                        is ApiResult.Success -> refresh()
                    }
                }
            }
        }
    }

    override fun onOutput(output: CollectionsListComponent.Output) {
        onOutput.invoke(output)
    }

    override fun refresh() {
        getCollections(sections)
    }

    private fun getCollections(
        sections: Array<SectionWithQuery>
    ) {
        coroutineScope.launch {
            _state.update {
                it.copy(
                    list = emptyList(),
                    isLoading = true
                )
            }
            val results = sections.map { section ->
                collectionsRepo.get(section, 1)
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