package ru.blays.ficbookReader.shared.ui.landingScreenComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface LandingScreenComponent {
    val confirmDialog: Value<ChildSlot<ConfirmDialogConfig, ConfirmDialogComponent>>

    fun sendIntent(intent: Intent)

    sealed class Output {
        data object OpenLogInScreen: Output()
        data object Close: Output()
    }

    sealed class Intent {
        data object AddNewAccount: Intent()
        data object EnableAnonymousMode: Intent()
        data object Register: Intent()
        data object CloseDialog : Intent()

    }
}

class ConfirmDialogComponent(
    componentContext: ComponentContext,
    private val onConfirm: () -> Unit,
    private val onCancel: () -> Unit
): ComponentContext by componentContext {
    fun sendIntent(intent: Intent) {
        when(intent) {
            Intent.Cancel -> onCancel()
            Intent.Confirm -> onConfirm()
        }
    }

    sealed class Intent {
        data object Confirm: Intent()
        data object Cancel: Intent()
    }
}

@Serializable
sealed class ConfirmDialogConfig {
    @Serializable
    data object ConfirmAnonymousMode: ConfirmDialogConfig()
    @Serializable
    data object ConfirmRegisterRedirect: ConfirmDialogConfig()
    @Serializable
    data object ConfirmDBMigration: ConfirmDialogConfig()
    @Serializable
    data object DBMigrationFailed: ConfirmDialogConfig()
}
