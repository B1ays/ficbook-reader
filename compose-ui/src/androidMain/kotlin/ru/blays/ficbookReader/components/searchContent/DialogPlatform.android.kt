package ru.blays.ficbookReader.components.searchContent

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Suppress("DEPRECATION")
@Composable
actual fun DialogPlatform(
    modifier: Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        content = content
    )
}