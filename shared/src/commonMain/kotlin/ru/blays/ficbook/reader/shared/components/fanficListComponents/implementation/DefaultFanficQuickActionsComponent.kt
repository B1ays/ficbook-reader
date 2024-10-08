package ru.blays.ficbook.reader.shared.components.fanficListComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficQuickActionsComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficQuickActionsRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue

class DefaultFanficQuickActionsComponent(
    componentContext: ComponentContext,
    private val fanficID: String,
    private val fanficName: String,
    private val onFanficBan: (fanficID: String) -> Unit
): FanficQuickActionsComponent, ComponentContext by componentContext, KoinComponent {
    private val actionsRepo: IFanficPageRepo by inject()
    private val infoRepo: IFanficQuickActionsRepo by inject()
    private val filtersRepo: IFiltersRepo by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var initialized: Boolean = false

    private val _state = SaveableMutableValue(
        serializer = FanficQuickActionsComponent.State.serializer(),
        initialValue = FanficQuickActionsComponent.State(
            liked = false,
            subscribed = false,
            readed = false,
            loading = false,
            error = false,
            errorMessage = null
        )
    )

    override val state get() = _state

    override fun sendIntent(intent: FanficQuickActionsComponent.Intent) {
        when(intent) {
            FanficQuickActionsComponent.Intent.Initialize -> {
                if(!initialized) {
                    coroutineScope.launch { loadData() }
                }
            }
            FanficQuickActionsComponent.Intent.Like -> {
                coroutineScope.launch {
                    val newValue = !state.value.liked
                    val success = actionsRepo.mark(
                        mark = newValue,
                        fanficID = fanficID
                    )
                    if(success) {
                        _state.update {
                            it.copy(liked = newValue)
                        }
                    }
                }
            }
            FanficQuickActionsComponent.Intent.Read -> {
                coroutineScope.launch {
                    val newValue = !state.value.readed
                    val success = actionsRepo.read(
                        read = newValue,
                        fanficID = fanficID
                    )
                    if(success) {
                        _state.update {
                            it.copy(readed = newValue)
                        }
                    }
                }
            }
            FanficQuickActionsComponent.Intent.Subscribe -> {
                coroutineScope.launch {
                    val newValue = !state.value.subscribed
                    val success = actionsRepo.follow(
                        follow = newValue,
                        fanficID = fanficID
                    )
                    if(success) {
                        _state.update {
                            it.copy(subscribed = newValue)
                        }
                    }
                }
            }
            FanficQuickActionsComponent.Intent.Ban -> {
                coroutineScope.launch {
                    filtersRepo.addFanficToBlacklist(fanficID, fanficName)
                    onFanficBan(fanficID)
                }
            }
        }
    }

    private suspend fun loadData() {
        _state.update {
            it.copy(loading = true)
        }
        when(
            val result = infoRepo.getInfo(fanficID)
        ) {
            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = true,
                        errorMessage = result.exception.message
                    )
                }
                result.exception.printStackTrace()
            }
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = false,
                        liked = result.value.liked,
                        subscribed = result.value.subscribed,
                        readed = result.value.readed
                    )
                }
                initialized = true
            }
        }
    }

    init {
        lifecycle.doOnDestroy(coroutineScope::cancel)
    }
}