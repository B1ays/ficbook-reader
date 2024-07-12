package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ficbook_reader.compose_ui.generated.resources.Res
import ficbook_reader.compose_ui.generated.resources.ic_battery

import org.jetbrains.compose.resources.painterResource
import ru.blays.ficbook.platformUtils.rememberBatteryObserver
import ru.blays.ficbook.platformUtils.rememberTimeObserver

@Composable
fun ReaderBottomContent(
    pagerState: PagerState,
    modifier: Modifier
) {
    val time by rememberTimeObserver()
    val batteryCapacity by rememberBatteryObserver()

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.Center,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.background, //TODO "surfaceContainerLowest"
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp
                )
            ),
    ) {
        item {
            Text(
                text = "${pagerState.currentPage percentageOf pagerState.pageCount}%",
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = "${(pagerState.currentPage)+1}/${pagerState.pageCount}",
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        item {
            Text(
                text = time,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_battery),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$batteryCapacity%",
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

        }
    }
}