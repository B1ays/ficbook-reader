package ru.blays.ficbook.reader.shared.components.fanficListComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficQuickActionsRepo
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficQuickActionsComponent

class DefaultFanficQuickActionsComponent(
    componentContext: ComponentContext,
    private val fanficID: String
): FanficQuickActionsComponent, ComponentContext by componentContext {
    private val actionsRepo: IFanficPageRepo by getKoin().inject()
    private val infoRepo: IFanficQuickActionsRepo by getKoin().inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var initialized: Boolean = false

    private val _state = MutableValue(
        FanficQuickActionsComponent.State(
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
                        _state.value = state.value.copy(liked = newValue)
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
                        _state.value = state.value.copy(readed = newValue)
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
                        _state.value = state.value.copy(subscribed = newValue)
                    }
                }
            }
        }
    }

    private suspend fun loadData() {
        _state.value = state.value.copy(loading = true)
        when(
            val result = infoRepo.getInfo(fanficID)
        ) {
            is ApiResult.Error -> {
                _state.value = state.value.copy(
                    loading = false,
                    error = true,
                    errorMessage = result.exception.message
                )
                result.exception.printStackTrace()
            }
            is ApiResult.Success -> {
                _state.value = state.value.copy(
                    loading = false,
                    error = false,
                    liked = result.value.liked,
                    subscribed = result.value.subscribed,
                    readed = result.value.readed
                )
                initialized = true
            }
        }
    }

    init {
        lifecycle.doOnDestroy(coroutineScope::cancel)
    }
}