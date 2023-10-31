@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package ru.blays.ficbookReader.ui_components.FanficComponents

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
    onCardClick: () -> Unit,
    onPairingClick: (pairing: PairingModelStable) -> Unit,
    onFandomClick: (fandom: FandomModelStable) -> Unit,
    onAuthorClick: (author: UserModelStable) -> Unit
) {
    val windowSize = WindowSize()

    if(windowSize.width > 700) {
        LandscapeContent(
            modifier = modifier,
            fanfic = fanfic,
            onCardClick = onCardClick,
            onPairingClick = onPairingClick,
            onFandomClick = onFandomClick,
            onAuthorClick = onAuthorClick
        )
    } else {
        PortraitContent(
            modifier = modifier,
            fanfic = fanfic,
            onCardClick = onCardClick,
            onPairingClick = onPairingClick,
            onFandomClick = onFandomClick,
            onAuthorClick = onAuthorClick
        )
    }
}

@Composable
private fun LandscapeContent(
    modifier: Modifier = Modifier,
    fanfic: FanficCardModelStable,
    onCardClick: () -> Unit,
    onPairingClick: (pairing: PairingModelStable) -> Unit,
    onFandomClick: (fandom: FandomModelStable) -> Unit,
    onAuthorClick: (author: UserModelStable) -> Unit
) {
    val status = fanfic.status

    BoxWithConstraints {
        val widthFill = when(constraints.maxWidth) {
            in 2000..Int.MAX_VALUE -> 0.65F
            in 1600..2000 -> 0.7F
            in 1300..1600 -> 0.8F
            in 900..1300 -> 0.9F
            else -> 1F
        }
        CardWithDirectionIndicator(
            direction = status.direction,
            modifier = modifier.fillMaxWidth(widthFill),
            onClick = onCardClick
        ) {
            println("Element width: ${constraints.maxWidth}")

            Column(
                modifier = Modifier.fillMaxWidth(widthFill)
            ) {
                FanficChips(status)
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPaddingSmall)
                        .fillMaxWidth()
                ) {
                    if (fanfic.coverUrl.isNotEmpty()) {
                        KamelImage(
                            modifier = Modifier
                                .layout { measurable, constraints ->
                                    val widthFloat = (constraints.maxWidth * 0.38F).coerceAtMost(300F)
                                    val heightFloat = widthFloat * 1.5F

                                    layout(
                                        width = widthFloat.toInt(),
                                        height = heightFloat.toInt()
                                    ) {
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
                    FanficHeader(
                        fanfic = fanfic,
                        onPairingClick = onPairingClick,
                        onFandomClick = onFandomClick,
                        onAuthorClick = onAuthorClick
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = fanfic.description
                )
            }
        }
    }
}

@Composable
private fun PortraitContent(
    modifier: Modifier = Modifier,
    fanfic: FanficCardModelStable,
    onCardClick: () -> Unit,
    onPairingClick: (pairing: PairingModelStable) -> Unit,
    onFandomClick: (fandom: FandomModelStable) -> Unit,
    onAuthorClick: (author: UserModelStable) -> Unit
) {
    val status = fanfic.status

    Column(
        modifier = modifier
    ) {
        KamelImage(
            modifier = Modifier
                .offset(y = 16.dp)
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
                .fillMaxWidth(),
            onClick = onCardClick
        ) {
            Column {
                FanficChips(status)
                FanficHeader(
                    fanfic = fanfic,
                    onPairingClick = onPairingClick,
                    onFandomClick = onFandomClick,
                    onAuthorClick = onAuthorClick
                )
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
    val density = LocalDensity.current
    val indicatorWidthInDp = 10.dp
    val indicatorWidthInPx = with(density) { indicatorWidthInDp.roundToPx() }
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
                it.measure(
                    constraints.copy(
                        maxWidth = constraints.maxWidth - indicatorWidthInPx,
                        minWidth = 0
                    )
                )
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
                        width = indicatorWidthInPx,
                        height = maxHeight.height
                    )
                )
            }

            layout(width = constraints.maxWidth, height = maxHeight.height) {
                val firstIndicator =  indicator.first()
                firstIndicator.place(0, 0)
                info.first().placeRelative(indicatorWidthInPx, 0)
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun FanficHeader(
    fanfic: FanficCardModelStable,
    onPairingClick: (pairing: PairingModelStable) -> Unit,
    onFandomClick: (fandom: FandomModelStable) -> Unit,
    onAuthorClick: (author: UserModelStable) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPaddingSmall)
    ) {
        Text(
            text = fanfic.title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                // TODO
            }
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                onFandomClick(fanfic.fandom)
            }
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(Res.image.ic_open_book),
                contentDescription = "Иконка открытая книга"
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = fanfic.fandom.name,
                style = MaterialTheme.typography.labelLarge
            )
        }
        if(fanfic.pairings.isNotEmpty()) {
            FlowRow {
                val shape = remember { RoundedCornerShape(percent = 20) }
                val style = MaterialTheme.typography.labelLarge
                Text(
                    text = "Пэйринги и персонажи:",
                    style = style
                )
                Spacer(modifier = Modifier.requiredWidth(3.dp))
                fanfic.pairings.forEach { pairing ->
                    Text(
                        text = pairing.character + ',',
                        style = style,
                        color = if (pairing.isHighlighted) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            Color.Unspecified
                        },
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(2.dp)
                            .background(
                                color = if (pairing.isHighlighted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Unspecified
                                },
                                shape = shape
                            )
                            .clip(shape)
                            .clickable {
                                onPairingClick(pairing)
                            }
                    )
                    Spacer(modifier = Modifier.requiredWidth(3.dp))
                }
            }
        }
        Spacer(modifier = Modifier.requiredHeight(2.dp))
        Text(
            text = "Тэги:",
            style = MaterialTheme.typography.labelLarge
        )
        FlowRow {
            fanfic.tags.forEach { tag ->
                FanficTagChip(tag = tag)
            }
        }
        Spacer(modifier = Modifier.requiredHeight(4.dp))
        Text(
            text = "Обновлено: ${fanfic.updateDate}",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.requiredHeight(4.dp))
        Text(
            text = "Размер: ${fanfic.size}",
            style = MaterialTheme.typography.labelLarge
        )
    }
}