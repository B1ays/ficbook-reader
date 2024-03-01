package ru.blays.ficbook.components.searchContent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
actual fun DialogPlatform(
    modifier: Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true
        ),
        alignment = Alignment.Center,
        content = content
    )
}