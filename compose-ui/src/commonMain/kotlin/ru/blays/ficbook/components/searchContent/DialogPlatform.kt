package ru.blays.ficbook.components.searchContent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun DialogPlatform(
    modifier: Modifier = Modifier,
    dismissOnClickOutside: Boolean = true,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
)