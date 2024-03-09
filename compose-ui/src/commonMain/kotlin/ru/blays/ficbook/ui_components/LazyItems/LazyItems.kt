package ru.blays.ficbook.ui_components.LazyItems

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

fun LazyListScope.itemWithHeader(title: String, content: @Composable () -> Unit) = item {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        text = title,
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Start
    )
    content()
    Spacer(modifier = Modifier.height(16.dp))
}

inline fun <T> LazyListScope.items(
    items: Set<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable LazyItemScope.(item: T) -> Unit
) = items(
    count = items.size,
    key = if (key != null) { index: Int -> key(items.elementAt(index)) } else null,
    contentType = { index: Int -> contentType(items.elementAt(index)) }
) {
    itemContent(items.elementAt(it))
}

