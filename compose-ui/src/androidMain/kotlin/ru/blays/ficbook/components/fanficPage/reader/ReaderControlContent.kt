package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ficbook_reader.compose_ui.generated.resources.Res
import ficbook_reader.compose_ui.generated.resources.ic_book_outlined
import ficbook_reader.compose_ui.generated.resources.ic_clock
import ficbook_reader.compose_ui.generated.resources.ic_settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.VoteReaderComponent
import ru.blays.ficbook.ui_components.FanficComponents.CircleChip
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding

@Suppress("AnimateAsStateLabel")
@Composable
fun ReaderControl(
    voteComponent: VoteReaderComponent,
    pagerState: PagerState,
    readerState: MainReaderComponent.State,
    eventSource: Flow<Boolean>,
    modifier: Modifier = Modifier,
    openNextChapter: () -> Unit,
    openPreviousChapter: () -> Unit,
    openSettings: () -> Unit
) {
    val voteState by voteComponent.state.subscribeAsState()

    val hasPreviousPage = pagerState.canScrollBackward
    val hasNextPage = pagerState.canScrollForward
    val hasNextChapter = readerState.chapterIndex < readerState.chaptersCount-1
    val hasPreviousChapter = readerState.chapterIndex > 0

    val previousButtonActive = hasPreviousChapter && !hasPreviousPage
    val nextButtonActive = hasNextChapter && !hasNextPage

    val scope = rememberCoroutineScope()

    var expanded by remember { mutableStateOf(false) }

    val shape = CardDefaults.shape

    LaunchedEffect(previousButtonActive, nextButtonActive, hasNextPage) {
        if(
            !readerState.settings.autoOpenNextChapter &&
            (previousButtonActive ||
                    nextButtonActive ||
                    !hasNextChapter && !hasNextPage)
        ) {
            expanded = true
        }
    }

    LaunchedEffect(Unit) {
        eventSource.collect { newValue ->
            expanded = newValue
        }
    }

    AnimatedVisibility(
        visible = expanded,
        enter = slideInVertically(spring()) { it/2 }
                + expandVertically(spring()),
        exit = slideOutVertically(spring()) { it/2 }
                + shrinkVertically(spring()),
    ) {
        Column {
            AnimatedVisibility(
                visible = !hasNextChapter && !hasNextPage,
                enter = fadeIn(spring()),
                exit = fadeOut(spring()),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if(voteState.canVote) {
                        val backgroundColor by animateColorAsState(
                            targetValue = if(voteState.votedForContinue) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            animationSpec = spring()
                        )
                        val contentColor by animateColorAsState(
                            targetValue = if(voteState.votedForContinue) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                        CircleChip(
                            color = backgroundColor,
                            modifier = Modifier.toggleable(
                                value = voteState.votedForContinue,
                                onValueChange = { newValue ->
                                    voteComponent.sendIntent(
                                        VoteReaderComponent.Intent.VoteForContinue(
                                            vote = newValue
                                        )
                                    )
                                }
                            )
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_clock),
                                contentDescription = "Проголосовать за продолжение",
                                tint = contentColor,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Жду продолжения",
                                style = MaterialTheme.typography.labelLarge,
                                color = contentColor
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                    }
                    val backgroundColor by animateColorAsState(
                        targetValue = if(voteState.readed) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        animationSpec = spring()
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if(voteState.readed) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    CircleChip(
                        color = backgroundColor,
                        modifier = Modifier.toggleable(
                            value = voteState.readed,
                            onValueChange = { newValue ->
                                voteComponent.sendIntent(
                                    VoteReaderComponent.Intent.Read(
                                        read = newValue
                                    )
                                )
                            }
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_book_outlined),
                            contentDescription = "Прочитанно",
                            tint = contentColor,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Прочитано",
                            style = MaterialTheme.typography.labelLarge,
                            color = contentColor
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.requiredHeight(8.dp))
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.4F)
                    .defaultMinSize(
                        minHeight = 35.dp
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(onClick = openSettings)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp).align(Alignment.Center),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_settings),
                        contentDescription = "Иконка настроек",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.requiredWidth(6.dp))
                    Text(
                        text = "Настройки",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.requiredHeight(15.dp))
            Row(
                modifier = modifier
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = shape
                    )
                    .clip(shape)
                    .padding(DefaultPadding.CardDefaultPaddingSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChangeChapterButton(
                    modifier = Modifier.weight(1F / 5F).padding(6.dp),
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    enabled = previousButtonActive,
                    onClick = openPreviousChapter
                )
                Slider(
                    modifier = Modifier.weight(3F / 5F).padding(horizontal = 3.dp),
                    value = pagerState.currentPage.toFloat(),
                    onValueChange = {
                        scope.launch {
                            pagerState.scrollToPage(it.toInt())
                        }
                    },
                    valueRange = 0F..(pagerState.pageCount - 1)
                        .coerceAtLeast(1)
                        .toFloat()
                )
                ChangeChapterButton(
                    modifier = Modifier.weight(1F / 5F).padding(6.dp),
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    enabled = nextButtonActive,
                    onClick = openNextChapter
                )
            }
        }
    }
}

@Suppress("AnimateAsStateLabel")
@Composable
private fun ChangeChapterButton(
    modifier: Modifier,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if(enabled) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            val primaryContainer = MaterialTheme.colorScheme.primaryContainer.toArgb()
            val surfaceColor = Color.Gray.toArgb()
            val blendedArgb = ColorUtils.blendARGB(primaryContainer, surfaceColor, 0.6f)
            Color(blendedArgb)
        }
    )
    val shape = CardShape.CardStandaloneLarge

    Row(
        modifier = modifier
            .layout { measurable, constraints ->
                val size = constraints.maxWidth.coerceAtMost(200)
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = size,
                        maxWidth = size,
                        minHeight = size,
                        maxHeight = size
                    )
                )
                layout(size, size) {
                    placeable.place(0, 0)
                }
            }
            .clickable(
                onClick = onClick,
                enabled = enabled
            )
            .background(
                color = containerColor,
                shape = shape
            )
            .clip(shape),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}