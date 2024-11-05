package ru.blays.ficbook.reader.shared.components.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageActionsComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageCollectionsComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.InternalFanficPageActionsComponent
import ru.blays.ficbook.reader.shared.data.dto.AvailableCollectionsModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICollectionsRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficPageRepo

class DefaultFanficPageActionsComponent(
    componentContext: ComponentContext,
    private val output: (output: FanficPageActionsComponent.Output) -> Unit,
) : InternalFanficPageActionsComponent, ComponentContext by componentContext, KoinComponent {
    private val repository: IFanficPageRepo by inject()

    private val navigation = SlotNavigation<FanficPageActionsComponent.ChildConfig>()

    private val _state = MutableValue(FanficPageActionsComponent.State())

    override val state: Value<FanficPageActionsComponent.State>
        get() = _state

    override val slot = childSlot(
        source = navigation,
        serializer = FanficPageActionsComponent.ChildConfig.serializer(),
        initialConfiguration = { null },
        handleBackButton = false,
        childFactory = ::childSlotFactory,
    )

    private var fanficID: String = ""

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: FanficPageActionsComponent.Intent) {
        when (intent) {
            is FanficPageActionsComponent.Intent.Follow -> follow(intent.follow)
            is FanficPageActionsComponent.Intent.Mark -> mark(intent.mark)
            is FanficPageActionsComponent.Intent.OpenAvailableCollections -> {
                navigation.activate(
                    FanficPageActionsComponent.ChildConfig(
                        fanficId = fanficID
                    )
                )
            }

            is FanficPageActionsComponent.Intent.CloseAvailableCollections -> {
                navigation.dismiss()
            }
        }
    }

    override fun onOutput(output: FanficPageActionsComponent.Output) {
        this.output(output)
    }

    override fun setFanficID(id: String) {
        fanficID = id
    }

    override fun setValue(value: FanficPageActionsComponent.State) {
        _state.update { value }
    }

    private fun follow(follow: Boolean) {
        coroutineScope.launch {
            val isSuccess = repository.follow(
                follow,
                fanficID
            )
            if(isSuccess) {
                _state.update {
                    it.copy(follow = follow)
                }
            }
        }
    }

    private fun mark(mark: Boolean) {
        coroutineScope.launch {
            val isSuccess = repository.mark(
                mark,
                fanficID
            )
            if(isSuccess) {
                _state.update {
                    it.copy(mark = mark)
                }
            }
        }
    }

    private fun childSlotFactory(
        config: FanficPageActionsComponent.ChildConfig,
        childContext: ComponentContext,
    ): FanficPageCollectionsComponent {
        return DefaultFanficPageCollectionsComponent(
            componentContext = childContext,
            fanficId = config.fanficId
        )
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}

class DefaultFanficPageCollectionsComponent(
    componentContext: ComponentContext,
    private val fanficId: String,
) : FanficPageCollectionsComponent,
    KoinComponent,
    ComponentContext by componentContext {
    private val repository: ICollectionsRepo by inject()

    private val _state = MutableValue(
        FanficPageCollectionsComponent.State(
            availableCollections = null,
            loading = false,
            error = false,
            errorMessage = null
        )
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    override val state
        get() = _state

    override fun sendIntent(intent: FanficPageCollectionsComponent.Intent) {
        when (intent) {
            is FanficPageCollectionsComponent.Intent.AddToCollection -> scope.launch {
                addToCollection(
                    add = intent.add,
                    collection = intent.collection
                )
            }
        }
    }

    private suspend fun addToCollection(
        add: Boolean,
        collection: AvailableCollectionsModel.Data.Collection,
    ) = coroutineScope {
        val collectionID = collection.id.toString()
        val success = repository.addToCollection(
            add = add,
            collectionID = collectionID,
            fanficID = fanficId
        )
        if(success) {
            val availableCollections = state.value.availableCollections!!
            val collections = availableCollections.data.collections
            val newCollections = collections.map { collection ->
                if (collection.id.toString() == collectionID) {
                    collection.copy(
                        isInThisCollection = if (add) {
                            AvailableCollectionsModel.Data.Collection.IN_COLLECTION
                        } else {
                            AvailableCollectionsModel.Data.Collection.NOT_IN_COLLECTION
                        },
                        count = if (add) collection.count + 1 else collection.count - 1
                    )
                } else {
                    collection
                }
            }
            _state.update {
                it.copy(
                    availableCollections = availableCollections.copy(
                        data = availableCollections.data.copy(
                            collections = newCollections
                        )
                    )
                )
            }
        }
    }

    private suspend fun loadAvailableCollections() = coroutineScope {
        _state.update {
            it.copy(loading = true)
        }
        when (
            val result = repository.getAvailableCollections(fanficId)
        ) {
            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = true,
                        errorMessage = result.exception.message
                    )
                }
            }
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        availableCollections = result.value,
                        loading = false,
                        error = false,
                    )
                }
            }
        }
    }

    init {
        lifecycle.doOnCreate {
            scope.launch {
                loadAvailableCollections()
            }
        }
        lifecycle.doOnDestroy {
            scope.cancel()
        }
    }
}