/*
package ru.blays.ficbookreader.ui.components.FanficComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import ru.blays.ficbookReader.shared.data.dto.FanficCompletionStatus
import ru.blays.ficbookReader.shared.data.dto.FanficRating
import ru.blays.ficbookreader.R
import ru.blays.ficbookReader.ui_components.GradientIcon.GradientIcon
import ru.blays.ficbookReader.theme.getFlameGradient
import ru.blays.ficbookReader.theme.getColorForDirection
import ru.blays.ficbookReader.theme.getColorForStatus
import ru.blays.ficbookReader.theme.getLikeColor
import ru.blays.ficbookReader.theme.getTrophyColor
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
fun a() {
    Column(
        modifier = modifier
    ) {
        if(fanfic.coverUrl.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .offset(y = 16.dp)
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp
                        )
                    )
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth(),
                    model = fanfic.coverUrl,
                    contentDescription = "Обложка фанфика",
                    contentScale = ContentScale.Crop
                )
                if(fanfic.readInfo != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(
                                horizontal = 14.dp,
                                vertical = 6.dp
                            ),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceTint
                                        .copy(alpha = 0.6F),
                                    shape = CircleShape
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if(fanfic.readInfo!!.hasUpdate) {
                                Text(
                                    modifier = Modifier.padding(
                                        vertical = 3.dp,
                                        horizontal = 6.dp
                                    ),
                                    text = "Прочитано: ${fanfic.readInfo!!.readDate}",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceTint
                                        .copy(alpha = 0.6F),
                                    shape = CircleShape
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if(fanfic.readInfo!!.hasUpdate) {
                                Text(
                                    modifier = Modifier.padding(
                                        vertical = 3.dp,
                                        horizontal = 6.dp
                                    ),
                                    text = "Есть обновления",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                }
            }
        } else {
            if(fanfic.readInfo != null) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.8F)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    .copy(alpha = 0.6F),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp
                                )
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if(fanfic.readInfo!!.hasUpdate) {
                            Text(
                                modifier = Modifier.padding(
                                    vertical = 2.dp,
                                    horizontal = 4.dp
                                ),
                                text = "Есть обновления",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                modifier = Modifier.padding(
                                    vertical = 1.dp,
                                    horizontal = 4.dp
                                ),
                                text = "Прочитано: ${fanfic.readInfo!!.readDate}",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val (directionIndicator, info) = createRefs()
                Box(
                    modifier = Modifier
                        .constrainAs(directionIndicator) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        }
                        .fillMaxHeight()
                        .fillMaxWidth(0.03F)
                        .background(
                            color = getColorForDirection(status.direction)
                        )
                )

                Column(
                    modifier = Modifier
                        .constrainAs(info) {
                            top.linkTo(parent.top)
                            start.linkTo(directionIndicator.end, margin = 10.dp)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                ) {
                    FlowRow(
                        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (status.rating != FanficRating.UNKNOWN ) {
                            CircleChip(
                                color = MaterialTheme.colorScheme.surfaceContainer,
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
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                minSize = 27.dp
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(3.dp)
                                        .size(20.dp),
                                    imageVector = when(status.status) {
                                        FanficCompletionStatus.IN_PROGRESS -> ImageVector.vectorResource(
                                            R.drawable.ic_clock)
                                        FanficCompletionStatus.COMPLETE -> ImageVector.vectorResource(
                                            R.drawable.ic_check)
                                        FanficCompletionStatus.FROZEN -> ImageVector.vectorResource(
                                            R.drawable.ic_snowflake)
                                        FanficCompletionStatus.UNKNOWN -> Icons.Rounded.Close
                                    },
                                    contentDescription = "Значок статуса",
                                    tint = getColorForStatus(status.status)

                                )
                            }
                        }
                        if (status.likes != 0) {
                            CircleChip(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                minSize = 27.dp
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(3.dp)
                                        .size(20.dp),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_like_outlined),
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
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                minSize = 27.dp
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(3.dp)
                                        .size(20.dp),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_trophy),
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
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                minSize = 27.dp
                            ) {
                                GradientIcon(
                                    modifier = Modifier
                                        .padding(3.dp)
                                        .size(20.dp),
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_flame),
                                    contentDescription = "Значок огня",
                                    brush = flameGradient
                                )
                            }
                        }
                    }
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
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_user),
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
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_open_book),
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
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        modifier = Modifier
                            .padding(DefaultPadding.CardDefaultPaddingSmall),
                        text = fanfic.description
                    )
                }
            }
        }
    }
}*/

/*
*     val status = fanfic.status

    ConstraintLayout(
        modifier = modifier
    ) {
        val (
            cover,
            directionIndicator,
            card
        ) = createRefs()

        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(cover) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            model = fanfic.coverUrl,
            contentDescription = "Обложка фанфика",
            contentScale = ContentScale.Crop
        )

        Card(
            modifier = Modifier
                .constrainAs(card) {
                    top.linkTo(cover.bottom, )
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            onClick = onClick,
        ) {
            FlowRow(
                modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
                verticalArrangement = Arrangement.Center
            ) {
                if (status.rating != FanficRating.UNKNOWN ) {
                    CircleChip(
                        color = MaterialTheme.colorScheme.surfaceContainer,
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
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        minSize = 27.dp
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(20.dp),
                            imageVector = when(status.status) {
                                FanficCompletionStatus.IN_PROGRESS -> ImageVector.vectorResource(
                                    R.drawable.ic_clock)
                                FanficCompletionStatus.COMPLETE -> ImageVector.vectorResource(
                                    R.drawable.ic_check)
                                FanficCompletionStatus.FROZEN -> ImageVector.vectorResource(
                                    R.drawable.ic_snowflake)
                                FanficCompletionStatus.UNKNOWN -> Icons.Rounded.Close
                            },
                            contentDescription = "Значок статуса",
                            tint = getColorForStatus(status.status)

                        )
                    }
                }
                if (status.likes != 0) {
                    CircleChip(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        minSize = 27.dp
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(20.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_like_outlined),
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
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        minSize = 27.dp
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(20.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_trophy),
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
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        minSize = 27.dp
                    ) {
                        GradientIcon(
                            modifier = Modifier
                                .padding(3.dp)
                                .size(20.dp),
                            imageVector = ImageVector.vectorResource(R.drawable.ic_flame),
                            contentDescription = "Значок огня",
                            brush = flameGradient
                        )
                    }
                }
            }
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
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_user),
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
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_open_book),
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
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPaddingSmall),
                text = fanfic.description
            )
        }

        Box(
            modifier = Modifier
                .constrainAs(directionIndicator) {
                    top.linkTo(card.top)
                    start.linkTo(card.start)
                    bottom.linkTo(card.bottom)
                    height = Dimension.fillToConstraints
                }
                .fillMaxHeight()
                .fillMaxWidth(0.03F)
                .background(
                    color = getColorForDirection(status.direction), CardDefaults.shape
                )
        )
    }
* */
