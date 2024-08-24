package ru.blays.ficbook.ui_components.dialogComponents

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

@Suppress("DEPRECATION")
@Composable
actual fun DialogPlatform(
    modifier: Modifier,
    dismissOnClickOutside: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = dismissOnClickOutside
        ),
        modifier = modifier,
        content = content
    )
}