package ru.blays.ficbook.ui_components.dialogComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
actual fun DialogPlatform(
    modifier: Modifier,
    dismissOnClickOutside: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            dismissOnClickOutside = dismissOnClickOutside,
            focusable = true
        ),
        alignment = Alignment.Center,
        content = content
    )
}