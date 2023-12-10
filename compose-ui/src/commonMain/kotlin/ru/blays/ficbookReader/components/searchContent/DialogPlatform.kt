package ru.blays.ficbookReader.components.searchContent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun DialogPlatform(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
)