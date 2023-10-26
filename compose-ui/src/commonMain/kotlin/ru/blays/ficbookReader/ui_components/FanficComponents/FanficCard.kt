@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package ru.blays.ficbookReader.ui_components.FanficComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.myapplication.compose.Res
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import ru.blays.ficbookReader.platformUtils.WindowSize
import ru.blays.ficbookReader.shared.data.dto.*
import ru.blays.ficbookReader.theme.*
import ru.blays.ficbookReader.ui_components.GradientIcon.GradientIcon
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
fun FanficCard(
    fanfic: FanficCardModelStable,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val windowSize = WindowSize()

    if(windowSize.width > 700) {
        LandscapeContent(modifier, fanfic, onClick)
    } else {
        PortraitContent(modifier, fanfic, onClick)
    }
}

@Composable
private fun LandscapeContent(
    modifier: Modifier = Modifier,
    fanfic: FanficCardModelStable,
    onClick: () -> Unit
) {
    val status = fanfic.status
    CardWithDirectionIndicator(
        direction = status.direction,
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column {
            FanficChips(status)
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPaddingSmall)
                    .fillMaxWidth()
            ) {
                if(fanfic.coverUrl.isNotEmpty()) {
                    KamelImage(
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val widthFloat = (constraints.maxWidth*0.38F)
                                val heightFloat = widthFloat*1.5F

                                layout(width = widthFloat.toInt(), height = heightFloat.toInt()) {
                                    measurable.measure(
                                        Constraints(
                                            maxWidth = widthFloat.toInt(),
                                            maxHeight = heightFloat.toInt()
                                        )
                                    ).place(0, 0)
                                }
                            }
                            .clip(CardDefaults.shape),
                        resource = asyncPainterResource(fanfic.coverUrl),
                        contentDescription = "Обложка фанфика",
                        contentScale = ContentScale.Crop
                    )
                }
                FanficHeader(fanfic)
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = fanfic.description
            )
        }
    }
}

@Composable
private fun PortraitContent(
    modifier: Modifier = Modifier,
    fanfic: FanficCardModelStable,
    onClick: () -> Unit
) {
    val status = fanfic.status

    Column(
        modifier = modifier
    ) {
        KamelImage(
            modifier = Modifier
                .offset(y = 8.dp)
                .fillMaxWidth()
                .heightIn(
                    max = 250.dp
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp
                    )
                ),
            resource = asyncPainterResource(fanfic.coverUrl),
            contentDescription = "Обложка фанфика",
            contentScale = ContentScale.Crop
        )
        CardWithDirectionIndicator(
            direction = status.direction,
            modifier = Modifier
                .offset(y = (-8).dp)
                .fillMaxWidth(),
            onClick = onClick
        ) {
            Column {
                FanficChips(status)
                FanficHeader(fanfic)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = fanfic.description
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardWithDirectionIndicator(
    direction: FanficDirection,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        SubcomposeLayout(
            modifier = Modifier.fillMaxWidth()
        ) { constraints ->

            val info = subcompose(
                slotId = "Info",
                content = content
            ).map {
                    it.measure(constraints)
            }

            val maxHeight = info.fold(IntSize.Zero) { currentMax, placeable ->
                IntSize(
                    width = maxOf(currentMax.width, placeable.width),
                    height = maxOf(currentMax.height, placeable.height)
                )
            }

            val indicator = subcompose("Indicator") {
                Box(
                    modifier = Modifier.background(
                        color = getColorForDirection(direction)
                    )
                )
            }.map {
                it.measure(
                    Constraints.fixed(
                        width = (constraints.minWidth*0.025F).toInt(),
                        height = maxHeight.height
                    )
                )
            }

            layout(width = constraints.maxWidth, height = maxHeight.height) {
                val firstIndicator =  indicator.first()
                firstIndicator.place(0, 0)
                info.first().placeRelative(firstIndicator.width, 0)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FanficChips(
    status: FanficStatusStable
) {
    FlowRow(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding),
        verticalArrangement = Arrangement.Center
    ) {
        if (status.rating != FanficRating.UNKNOWN ) {
            CircleChip(
                color = MaterialTheme.colorScheme.background,  /*TODO surfaceContainer*/
                minSize = 27.dp
            ) {
                Text(
                    text = status.rating.rating,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        if (status.status != FanficCompletionStatus.UNKNOWN) {
            CircleChip(
                color = MaterialTheme.colorScheme.background,  /*TODO surfaceContainer*/
                minSize = 27.dp
            ) {
                Icon(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(20.dp),
                    painter = when(status.status) {
                        FanficCompletionStatus.IN_PROGRESS -> painterResource(Res.image.ic_clock)
                        FanficCompletionStatus.COMPLETE -> painterResource(Res.image.ic_check)
                        FanficCompletionStatus.FROZEN -> painterResource(Res.image.ic_snowflake)
                        FanficCompletionStatus.UNKNOWN -> rememberVectorPainter(Icons.Rounded.Close)
                    },
                    contentDescription = "Значок статуса",
                    tint = getColorForStatus(status.status)

                )
            }
        }
        if (status.likes != 0) {
            CircleChip(
                color = MaterialTheme.colorScheme.background,  /*TODO surfaceContainer*/
                minSize = 27.dp
            ) {
                Icon(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(20.dp),
                    painter = painterResource(Res.image.ic_like_outlined),
                    contentDescription = "Значок лайка",
                    tint = likeColor
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = status.likes.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
        }
        if (status.trophies != 0) {
            CircleChip(
                color = MaterialTheme.colorScheme.background,  /*TODO surfaceContainer*/
                minSize = 27.dp
            ) {
                Icon(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(20.dp),
                    painter = painterResource(Res.image.ic_trophy),
                    contentDescription = "Значок награды",
                    tint = trophyColor
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = status.trophies.toString(),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
        }
        if (status.hot) {
            CircleChip(
                color = MaterialTheme.colorScheme.background,  /*TODO surfaceContainer*/
                minSize = 27.dp
            ) {
                GradientIcon(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(20.dp),
                    painter = painterResource(Res.image.ic_flame),
                    contentDescription = "Значок огня",
                    brush = flameGradient
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FanficHeader(
    fanfic: FanficCardModelStable
) {
    Row(
        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(0.6F)
        ) {
            Text(
                text = fanfic.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.image.ic_user),
                    contentDescription = "Иконка человек"
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = fanfic.author,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.image.ic_open_book),
                    contentDescription = "Иконка открытая книга"
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = fanfic.fandom,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            FlowRow {
                fanfic.tags.forEach { tag ->
                    FanficTagChip(tag = tag)
                }
            }
            Spacer(modifier = Modifier.requiredHeight(6.dp))
            Row {
                Text(
                    text = "Обновлено: ",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = fanfic.updateDate,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}