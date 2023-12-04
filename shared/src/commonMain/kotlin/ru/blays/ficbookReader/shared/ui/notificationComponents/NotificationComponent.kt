package ru.blays.ficbookReader.shared.ui.notificationComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbookReader.shared.data.dto.NotificationCategoryStable
import ru.blays.ficbookReader.shared.data.dto.NotificationModelStable
import ru.blays.ficbookReader.shared.data.dto.NotificationType

interface NotificationComponent {
    val state: Value<State>

    val slot: Value<ChildSlot<ConfirmDialogConfig, NotificationConfirmDialogComponent>>

    fun onOutput(output: Output)
    fun sendIntent(intent: Intent)

    sealed class Intent {
        data object LoadNextPage: Intent()

        data class SelectCategory(val category: NotificationType): Intent()

        data object DeleteAll: Intent()

        data object ReadAll: Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenNotificationHref(val href: String) : Output()
    }

    data class State(
        val list: List<NotificationModelStable>,
        val availableCategories: List<NotificationCategoryStable>,
        val selectedCategory: NotificationType,
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?
    )

    @Serializable
    sealed class ConfirmDialogConfig {
        @Serializable
        data object Delete: ConfirmDialogConfig()

        @Serializable
        data object Read: ConfirmDialogConfig()
    }
}

class NotificationConfirmDialogComponent(
    componentContext: ComponentContext,
    val actionName: String,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
): ComponentContext by componentContext {

    fun onOutput(output: Output) {
        when(output) {
            Output.Confirm -> {
                onConfirm()
                onCancel()
            }
            Output.Cancel -> onCancel()
        }
    }
    sealed class Output {
        data object Confirm: Output()
        data object Cancel: Output()
    }
}