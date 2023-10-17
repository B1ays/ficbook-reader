package ru.blays.ficbookReader.components.fanficPage.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import ru.blays.ficbookReader.platformUtils.rememberTimeObserver
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent

@Composable
fun ReaderTopBarContent(
    component: MainReaderComponent,
    modifier: Modifier
) {
    val state by component.state.subscribeAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer, //TODO "surfaceContainerLowest"
                shape = RoundedCornerShape(
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${state.chapterIndex+1}/${state.chaptersCount}",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            modifier = Modifier,
            text = state.chapterName,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun ReaderBottomContent(
    currentPage: Int,
    pagesCount: Int,
    component: MainReaderComponent,
    modifier: Modifier
) {
    val time by rememberTimeObserver()
    val state by component.state.subscribeAsState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer, //TODO "surfaceContainerLowest"
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${currentPage percentageOf pagesCount}%",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = "${currentPage}/${pagesCount}",
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = time,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(12.dp))
    }
}

infix fun Int.percentageOf(value: Int): Int = ((this*100F)/value.toFloat()).toInt()