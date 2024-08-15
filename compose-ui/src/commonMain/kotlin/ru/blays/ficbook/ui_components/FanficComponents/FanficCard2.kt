package ru.blays.ficbook.ui_components.FanficComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atMost
import coil3.compose.AsyncImage
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.data.dto.*
import ru.blays.ficbook.theme.*
import ru.blays.ficbook.ui_components.GradientIcon.GradientIcon
import ru.blays.ficbook.ui_components.Text.HyperlinkText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FanficCard2(
    fanfic: FanficCardModelStable,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPairingClick: (pairing: PairingModelStable) -> Unit,
    onFandomClick: (fandom: FandomModelStable) -> Unit,
    onAuthorClick: (author: UserModelStable) -> Unit,
    onUrlClicked: (url: String) -> Unit
) {
    BoxWithConstraints {
        val useLandscapeLayout = maxWidth > 600.dp
        val coverAvailable = fanfic.coverUrl.isNotEmpty()
        val pairingsAvailable = fanfic.pairings.isNotEmpty()
        val tagsAvailable = fanfic.tags.isNotEmpty()
        val constraints = remember(useLandscapeLayout) {
            ConstraintSet {
                val background = createRefFor(LayoutIds.Background)
                val indicator = createRefFor(LayoutIds.Indicator)
                val cover = createRefFor(LayoutIds.Cover)
                val chipsRow = createRefFor(LayoutIds.ChipsRow)
                val title = createRefFor(LayoutIds.Title)
                val authors = createRefFor(LayoutIds.Authors)
                val fandoms = createRefFor(LayoutIds.Fandoms)
                val pairings = createRefFor(LayoutIds.Pairings)
                val tagsTitle = createRefFor(LayoutIds.TagsTitle)
                val tagsRow = createRefFor(LayoutIds.TagsRow)
                val updateInfo = createRefFor(LayoutIds.UpdateInfo)
                val sizeInfo = createRefFor(LayoutIds.SizeInfo)
                val description = createRefFor(LayoutIds.Description)

                val endGuideline = createGuidelineFromEnd(4.dp)

                if (coverAvailable) {
                    constrain(cover) {
                        if (useLandscapeLayout) {
                            top.linkTo(background.top, margin = 6.dp)
                            start.linkTo(indicator.end, margin = 4.dp)
                            bottom.linkTo(background.bottom, margin = 6.dp)
                            height = Dimension.ratio("0.6:1")
                            width = Dimension.fillToConstraints.atMost(300.dp)
                        } else {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                            height = Dimension.ratio("1:0.6")
                        }
                    }
                }

                constrain(background) {
                    if (coverAvailable && !useLandscapeLayout) {
                        top.linkTo(cover.bottom, margin = (-16).dp)
                    } else {
                        top.linkTo(parent.top)
                    }
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }

                constrain(indicator) {
                    top.linkTo(background.top)
                    bottom.linkTo(background.bottom)
                    start.linkTo(background.start)
                    height = Dimension.fillToConstraints
                    width = Dimension.value(10.dp)
                }

                constrain(chipsRow) {
                    top.linkTo(background.top, margin = 4.dp)
                    if(useLandscapeLayout && coverAvailable) {
                        start.linkTo(cover.end, 4.dp)
                    } else {
                        start.linkTo(indicator.end, 4.dp)
                    }
                    end.linkTo(endGuideline)
                    width = Dimension.fillToConstraints
                }

                constrain(title) {
                    top.linkTo(chipsRow.bottom, margin = 4.dp)
                    start.linkTo(chipsRow.start)
                    end.linkTo(endGuideline)
                    width = Dimension.fillToConstraints
                }

                constrain(authors) {
                    top.linkTo(title.bottom, margin = 4.dp)
                    start.linkTo(chipsRow.start)
                    end.linkTo(endGuideline)
                    width = Dimension.fillToConstraints
                }

                constrain(fandoms) {
                    top.linkTo(authors.bottom, margin = 4.dp)
                    start.linkTo(chipsRow.start)
                    end.linkTo(endGuideline)
                    width = Dimension.fillToConstraints
                }

                if(pairingsAvailable) {
                    constrain(pairings) {
                        top.linkTo(fandoms.bottom, margin = 4.dp)
                        start.linkTo(chipsRow.start)
                        end.linkTo(endGuideline)
                        width = Dimension.fillToConstraints
                    }
                }

                if(tagsAvailable) {
                    constrain(tagsTitle) {
                        if(pairingsAvailable) {
                            top.linkTo(pairings.bottom, margin = 4.dp)
                        } else {
                            top.linkTo(fandoms.bottom, margin = 4.dp)
                        }
                        start.linkTo(chipsRow.start)
                        end.linkTo(endGuideline)
                        width = Dimension.fillToConstraints
                    }

                    constrain(tagsRow) {
                        top.linkTo(tagsTitle.bottom)
                        start.linkTo(chipsRow.start)
                        end.linkTo(endGuideline)
                        width = Dimension.fillToConstraints
                    }
                }

                constrain(updateInfo) {
                    if(tagsAvailable) {
                        top.linkTo(tagsRow.bottom, margin = 4.dp)
                    } else if (pairingsAvailable) {
                        top.linkTo(pairings.bottom, margin = 4.dp)
                    } else {
                        top.linkTo(fandoms.bottom, margin = 4.dp)
                    }
                    start.linkTo(chipsRow.start)
                    end.linkTo(endGuideline)
                    width = Dimension.fillToConstraints
                }

                constrain(sizeInfo) {
                    top.linkTo(updateInfo.bottom, margin = 4.dp)
                    start.linkTo(chipsRow.start)
                    end.linkTo(endGuideline)
                    width = Dimension.fillToConstraints
                }

                constrain(description) {
                    top.linkTo(sizeInfo.bottom, margin = 2.dp)
                    start.linkTo(chipsRow.start)
                    end.linkTo(endGuideline)
                    width = Dimension.fillToConstraints
                }
            }
        }
        ConstraintLayout(
            constraintSet = constraints,
            animateChanges = true,
            modifier = modifier
                .fillMaxWidth()
                .clip(CardDefaults.shape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
        ) {
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            val cardColor = remember {
                if (fanfic.readInfo?.hasUpdate == true) {
                    lightGreen.copy(alpha = 0.3F).compositeOver(surfaceVariant)
                } else {
                    surfaceVariant
                }
            }


            if(useLandscapeLayout) {
                Box(
                    modifier = Modifier
                        .background(
                            color = cardColor,
                            shape = CardDefaults.shape
                        )
                        .layoutId(LayoutIds.Background)
                )
                AsyncImage(
                    model = fanfic.coverUrl,
                    contentDescription = stringResource(Res.string.content_description_fanfic_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .layoutId(LayoutIds.Cover)
                        .clip(CardDefaults.shape)
                )
            } else {
                AsyncImage(
                    model = fanfic.coverUrl,
                    contentDescription = stringResource(Res.string.content_description_fanfic_cover),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.layoutId(LayoutIds.Cover)

                )
                Box(
                    modifier = Modifier
                        .background(
                            color = cardColor,
                            shape = CardDefaults.shape
                        )
                        .layoutId(LayoutIds.Background)
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = fanfic.status.direction.color
                    )
                    .layoutId(LayoutIds.Indicator)
            )

            FanficChips(
                modifier = Modifier.layoutId(LayoutIds.ChipsRow),
                status = fanfic.status
            )

            Text(
                text = fanfic.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.layoutId(LayoutIds.Title),
            )

            FlowRow(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.layoutId(LayoutIds.Authors)
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.drawable.ic_user),
                    contentDescription = stringResource(Res.string.content_description_icon_user)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = fanfic.author.name,
                    style = MaterialTheme.typography.labelLarge,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        onAuthorClick(fanfic.author)
                    }
                )
                fanfic.originalAuthor?.let { originalAuthor ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(Res.drawable.ic_globe),
                        contentDescription = stringResource(Res.string.content_description_icon_globe)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = originalAuthor.name,
                        style = MaterialTheme.typography.labelLarge,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {}
                    )
                }
            }

            FlowRow(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.layoutId(LayoutIds.Fandoms),
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(Res.drawable.ic_open_book),
                    contentDescription = stringResource(Res.string.content_description_icon_book_opened)
                )
                Spacer(modifier = Modifier.width(4.dp))
                fanfic.fandom.forEachIndexed { index, it ->
                    Text(
                        text = it.name.let { if (index != fanfic.fandom.lastIndex) "$it," else it },
                        style = MaterialTheme.typography.labelLarge,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            onFandomClick(it)
                        }
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }

            if (fanfic.pairings.isNotEmpty()) {
                FlowRow(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.layoutId(LayoutIds.Pairings),
                ) {
                    val shape = remember { RoundedCornerShape(percent = 20) }
                    val style = MaterialTheme.typography.labelLarge
                    Text(
                        text = stringResource(Res.string.fanficCard_pairings_and_characters),
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

            if (fanfic.tags.isNotEmpty()) {
                Text(
                    text = stringResource(Res.string.fanficCard_tags),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.layoutId(LayoutIds.TagsTitle),
                )
                FlowRow(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.layoutId(LayoutIds.TagsRow),
                ) {
                    fanfic.tags.forEach { tag ->
                        FanficTagChip(tag = tag)
                    }
                }
            }

            Text(
                text = stringResource(Res.string.fanficCard_updated, fanfic.updateDate),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.layoutId(LayoutIds.UpdateInfo),
            )

            Text(
                text = stringResource(Res.string.fanficCard_size, fanfic.size),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.layoutId(LayoutIds.SizeInfo),
            )

            HyperlinkText(
                modifier = Modifier.layoutId(LayoutIds.Description),
                fullText = fanfic.description,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                linkTextColor = MaterialTheme.colorScheme.primary,
                linkTextDecoration = TextDecoration.Underline,
                overflow = TextOverflow.Ellipsis,
                maxLines = 6,
                onLinkClick = onUrlClicked
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FanficChips(
    modifier: Modifier = Modifier,
    status: FanficStatusStable
) {
    FlowRow(
        modifier = modifier,
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
                        FanficCompletionStatus.IN_PROGRESS -> painterResource(Res.drawable.ic_clock)
                        FanficCompletionStatus.COMPLETE -> painterResource(Res.drawable.ic_check)
                        FanficCompletionStatus.FROZEN -> painterResource(Res.drawable.ic_snowflake)
                        FanficCompletionStatus.UNKNOWN -> rememberVectorPainter(Icons.Rounded.Close)
                    },
                    contentDescription = stringResource(Res.string.content_description_icon_status),
                    tint = status.status.color
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
                    painter = painterResource(Res.drawable.ic_like_outlined),
                    contentDescription = stringResource(Res.string.content_description_icon_like),
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
                    painter = painterResource(Res.drawable.ic_trophy),
                    contentDescription = stringResource(Res.string.content_description_icon_reward),
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
                    painter = painterResource(Res.drawable.ic_flame),
                    contentDescription = stringResource(Res.string.content_description_icon_flame),
                    brush = flameGradient
                )
            }
        }
    }
}

private enum class LayoutIds {
    Background,
    Indicator,
    Cover,
    ChipsRow,
    Title,
    Authors,
    Fandoms,
    Pairings,
    TagsTitle,
    TagsRow,
    UpdateInfo,
    SizeInfo,
    Description,
}