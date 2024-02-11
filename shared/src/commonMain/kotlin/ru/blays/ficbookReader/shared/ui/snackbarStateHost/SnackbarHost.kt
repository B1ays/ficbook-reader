package ru.blays.ficbookReader.shared.ui.snackbarStateHost

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals

object SnackbarHost {
    private val _snackbarHostState: SnackbarHostState = SnackbarHostState()

    val snackbarHostState: SnackbarHostState
        get() = _snackbarHostState

    suspend fun showMessage(
        message: String,
        infoType: SnackbarMessageType = SnackbarMessageType.INFO
    ) {
        _snackbarHostState.showSnackbar(
            visuals = when (infoType) {
                SnackbarMessageType.ERROR -> DefaultSnackbarVisuals.SnackbarVisualsWithError(message)
                SnackbarMessageType.INFO -> DefaultSnackbarVisuals.SnackbarVisualsWithInfo(message)
            }
        )
    }
}

enum class SnackbarMessageType {
    ERROR,
    INFO;
}

sealed class DefaultSnackbarVisuals: SnackbarVisuals {
    class SnackbarVisualsWithError(
        override val message: String
    ) : DefaultSnackbarVisuals() {
        override val actionLabel: String = "Ок"
        override val withDismissAction: Boolean = true
        override val duration: SnackbarDuration
            get() = SnackbarDuration.Indefinite
    }

    class SnackbarVisualsWithInfo(
        override val message: String
    ) : DefaultSnackbarVisuals() {
        override val actionLabel = null
        override val withDismissAction = false
        override val duration: SnackbarDuration = SnackbarDuration.Short
    }
}

