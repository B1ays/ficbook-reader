package ru.blays.ficbook.reader.shared.components.notificationComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.saveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.NotificationType
import ru.blays.ficbook.reader.shared.data.repo.declaration.INotificationsRepo

@OptIn(ExperimentalStateKeeperApi::class)
class DefaultNotificationComponent(
    componentContext: ComponentContext,
    private val output: (output: NotificationComponent.Output) -> Unit
): NotificationComponent, ComponentContext by componentContext, KoinComponent {
    private val repository: INotificationsRepo by inject()

    private val _state = MutableValue(
        NotificationComponent.State(
            list = emptyList(),
            availableCategories = emptyList(),
            selectedCategory = NotificationType.ALL_NOTIFICATIONS,
            loading = false,
            error = false,
            errorMessage = null
        )
    )

    private val navigation = SlotNavigation<NotificationComponent.ConfirmDialogConfig>()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var nextPage: Int by saveable(
        serializer = Int.serializer(),
        key = NEXT_PAGE_KEY,
        init = { 1 }
    )

    override val state get() = _state

    override val slot = childSlot(
        source = navigation,
        serializer = NotificationComponent.ConfirmDialogConfig.serializer(),
        initialConfiguration = { null },
        handleBackButton = true,
        childFactory = ::childSlotFactory
    )

    override fun onOutput(output: NotificationComponent.Output) = this.output(output)

    override fun sendIntent(intent: NotificationComponent.Intent) {
        when(intent) {
            is NotificationComponent.Intent.DeleteAll -> navigation.activate(
                NotificationComponent.ConfirmDialogConfig.Delete
            )
            is NotificationComponent.Intent.LoadNextPage -> loadNextPage()
            is NotificationComponent.Intent.ReadAll -> navigation.activate(
                NotificationComponent.ConfirmDialogConfig.Read
            )
            is NotificationComponent.Intent.SelectCategory -> selectCategory(intent.category)
        }
    }

    private fun loadNextPage() {
        if(!state.value.loading) {
            coroutineScope.launch {
                _state.update {
                    it.copy(loading = true)
                }
                val result = repository.get(
                    category = state.value.selectedCategory,
                    page = nextPage
                )
                when(result) {
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
                                loading = false,
                                error = false,
                                list = it.list + result.value.list
                            )
                        }
                        nextPage += 1
                    }
                }
            }
        }
    }

    private fun selectCategory(category: NotificationType) {
        if(category != state.value.selectedCategory) {
            _state.update {
                it.copy(
                    list = emptyList(),
                    selectedCategory = category
                )
            }
            nextPage = 1
            loadNextPage()
        }

    }

    private fun getAvailableCategories() {
        coroutineScope.launch {
            when (
                val result = repository.getAvailableCategories()
            ) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            error = true,
                            errorMessage = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            availableCategories = result.value
                        )
                    }
                }
            }
        }
    }

    private fun deleteAll() {
        coroutineScope.launch {
            when(
                val result = repository.deleteAll()
            ) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            error = true,
                            errorMessage = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            list = emptyList(),
                            error = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    private fun readAll() {
        coroutineScope.launch {
            when (
                val result = repository.readAll()
            ) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            error = true,
                            errorMessage = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            error = false,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }

    private fun childSlotFactory(
        confirmDialogConfig: NotificationComponent.ConfirmDialogConfig,
        componentContext: ComponentContext
    ): NotificationConfirmDialogComponent {
        return when(confirmDialogConfig) {
            is NotificationComponent.ConfirmDialogConfig.Delete -> NotificationConfirmDialogComponent(
                componentContext = componentContext,
                actionName = "удалить всё",
                onConfirm = ::deleteAll,
                onCancel = navigation::dismiss
            )
            is NotificationComponent.ConfirmDialogConfig.Read -> NotificationConfirmDialogComponent(
                componentContext = componentContext,
                actionName = "прочитать все",
                onConfirm = ::readAll,
                onCancel = navigation::dismiss
            )
        }
    }

    fun init() {
        getAvailableCategories()
        loadNextPage()
    }

    init {
        lifecycle.doOnStart(true) {
            init()
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    companion object {
        private const val NEXT_PAGE_KEY = "next_page"
    }
}