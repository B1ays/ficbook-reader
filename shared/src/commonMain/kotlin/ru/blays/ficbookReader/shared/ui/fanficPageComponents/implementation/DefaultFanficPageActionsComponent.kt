package ru.blays.ficbookReader.shared.ui.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageActionsComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.InternalFanficPageActionsComponent

class DefaultFanficPageActionsComponent(
    componentContext: ComponentContext,
    private val output: (output: FanficPageActionsComponent.Output) -> Unit
): InternalFanficPageActionsComponent, ComponentContext by componentContext {
    private val repository: IFanficPageRepo by getKoin().inject()

    private val _state = MutableValue(FanficPageActionsComponent.State())
    override val state: Value<FanficPageActionsComponent.State>
        get() = _state

    private var fanficID: String = ""

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

     init {
         lifecycle.doOnDestroy {
             coroutineScope.cancel()
         }
     }

    override fun sendIntent(intent: FanficPageActionsComponent.Intent) {
        when(intent) {
            is FanficPageActionsComponent.Intent.Follow -> follow(intent.follow)
            is FanficPageActionsComponent.Intent.Mark -> mark(intent.mark)
        }
    }

    override fun onOutput(output: FanficPageActionsComponent.Output) {
        this.output(output)
    }

    override fun setFanficID(id: String) {
        this.fanficID = id
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
}