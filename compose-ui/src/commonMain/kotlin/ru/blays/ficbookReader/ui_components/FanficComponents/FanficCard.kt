package ru.blays.ficbookReader.ui_components.FanficComponents

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.myapplication.compose.Res
import ru.blays.ficbookReader.platformUtils.WindowSize
import ru.blays.ficbookReader.shared.data.dto.*
import ru.blays.ficbookReader.theme.*
import ru.blays.ficbookReader.ui_components.GradientIcon.GradientIcon
import ru.blays.ficbookReader.ui_components.HyperlinkText.HyperlinkText
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
fun FanficCard(
    fanfic: FanficCardModelStable,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit,
    onPairingClick: (pairing: PairingModelStable) -> Unit,
    onFandomClick: (fandom: FandomModelStable) -> Unit,
    onAuthorClick: (author: UserModelStable) -> Unit,
    onUrlClicked: (url: String) -> Unit
) {
    val windowSize = WindowSize()

    if(windowSize.width > 700) {
        LandscapeContent(
            modifier = modifier,
            fanfic = fanfic,
            onCardClick = onCardClick,
            onPairingClick = onPairingClick,
            onFandomClick = onFandomClick,
            onAuthorClick = onAuthorClick,
            onUrlClicked = onUrlClicked
        )
    } else {
        PortraitContent(
            modifier = modifier,
            fanfic = fanfic,
            onCardClick = onCardClick,
            onPairingClick = onPairingClick,
            onFandomClick = onFandomClick,
            onAuthorClick = onAuthorClick,
            onUrlClicked = onUrlClicked
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
    onAuthorClick: (author: UserModelStable) -> Unit,
    onUrlClicked: (url: String) -> Unit
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
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        val cardColor = remember {
            if (fanfic.readInfo?.hasUpdate == true) {
                lightGreen.copy(alpha = 0.3F).compositeOver(surfaceVariant)
            } else {
                surfaceVariant
            }
        }
        CardWithDirectionIndicator(
            direction = status.direction,
            modifier = modifier.fillMaxWidth(widthFill),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            onClick = onCardClick
        ) {
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
                        AsyncImage(
                            model = fanfic.coverUrl,
                            contentDescription = "Обложка фанфика",
                            contentScale = ContentScale.Crop,
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
                                .clip(CardDefaults.shape)
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
                HyperlinkText(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fullText = fanfic.description,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    linkTextColor = MaterialTheme.colorScheme.primary,
                    linkTextDecoration = TextDecoration.Underline,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 6,
                    onLinkClick = onUrlClicked,
                    onClick = onCardClick
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
    onAuthorClick: (author: UserModelStable) -> Unit,
    onUrlClicked: (url: String) -> Unit
) {
    val status = fanfic.status

    Column(
        modifier = modifier.clickable(onClick = onCardClick)
    ) {
        if(fanfic.coverUrl.isNotEmpty()) {
            AsyncImage(
                model = fanfic.coverUrl,
                contentDescription = "Обложка фанфика",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(y = 16.dp)
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )
                    )
            )
        }
        val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
        val cardColor = remember {
            if (fanfic.readInfo?.hasUpdate == true) {
                lightGreen.copy(alpha = 0.3F).compositeOver(surfaceVariant)
            } else {
                surfaceVariant
            }
        }
        CardWithDirectionIndicator(
            direction = status.direction,
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            modifier = Modifier.fillMaxWidth()
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
                HyperlinkText(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    fullText = fanfic.description,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    linkTextColor = MaterialTheme.colorScheme.primary,
                    linkTextDecoration = TextDecoration.Underline,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 6,
                    onLinkClick = onUrlClicked,
                    onClick = onCardClick
                )
            }
        }
    }
}

@Composable
fun CardWithDirectionIndicator(
    direction: FanficDirection,
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    border: BorderStroke? = null,
    shape: Shape = CardDefaults.shape,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val indicatorWidthInDp = 10.dp
    val indicatorWidthInPx = with(density) { indicatorWidthInDp.roundToPx() }
    Card(
        modifier = modifier.run {
            if(onClick != null) {
                this then clickable(onClick = onClick)
            } else this
        },
        colors = colors,
        border = border,
        shape = shape
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
        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
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
        FlowRow() {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(Res.image.ic_user),
                contentDescription = "Иконка человек"
            )
            Spacer(modifier = Modifier.width(2.dp))
            fanfic.author.forEach {
                Text(
                    text = it.name + ',',
                    style = MaterialTheme.typography.labelLarge,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        onAuthorClick(it)
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(Res.image.ic_open_book),
                contentDescription = "Иконка открытая книга"
            )
            Spacer(modifier = Modifier.width(4.dp))
            fanfic.fandom.forEachIndexed { index, it ->
                Text(
                    text = it.name.let { if(index != fanfic.fandom.lastIndex) "$it," else it },
                    style = MaterialTheme.typography.labelLarge,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        onFandomClick(it)
                    }
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        if(fanfic.pairings.isNotEmpty()) {
            FlowRow {
                val shape = remember { RoundedCornerShape(percent = 20) }
                val style = MaterialTheme.typography.labelLarge
                Text(
                    text = "Пэйринги и персонажи:",
                    style = style,
                    modifier = Modifier.align(Alignment.CenterVertically)
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
                        textDecoration = TextDecoration.Underline,
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
        if(fanfic.tags.isNotEmpty()) {
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
        }
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