package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent

@Composable
fun ReaderTopBarContent(
    modifier: Modifier = Modifier,
    state: MainReaderComponent.State
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${state.chapterIndex + 1}/${state.chaptersCount}",
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            modifier = Modifier,
            text = state.chapterName,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}