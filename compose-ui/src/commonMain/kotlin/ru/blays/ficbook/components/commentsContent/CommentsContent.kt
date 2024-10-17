package ru.blays.ficbook.components.commentsContent

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxOfOrNull
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.ExtendedCommentsComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.WriteCommentComponent
import ru.blays.ficbook.reader.shared.data.dto.CommentBlockModelStable
import ru.blays.ficbook.reader.shared.data.dto.CommentModelStable
import ru.blays.ficbook.reader.shared.data.dto.QuoteModelStable
import ru.blays.ficbook.theme.likeColor
import ru.blays.ficbook.ui_components.Text.HyperlinkText
import ru.blays.ficbook.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.LocalBlurState
import ru.blays.ficbook.utils.drawWithLayer
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.DefaultPadding
import ru.blays.ficbook.values.defaultScrollbarPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun CommentsContent(
    component: CommentsComponent,
    contentPadding: PaddingValues? = null,
    hideAvatar: Boolean = false,
    modifier: Modifier = Modifier,
    onAddReply: (
        userName: String,
        blocks: List<CommentBlockModelStable>
    ) -> Unit = { _, _ -> }
) {
    @Suppress("NAME_SHADOWING")
    val contentPadding = contentPadding ?: PaddingValues(0.dp)

    val state by component.state.subscribeAsState()
    val comments = state.comments
    val isLoading = state.loading

    val lazyListState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()

    val canScrollForward = lazyListState.canScrollForward
    val canScrollBackward = lazyListState.canScrollBackward

    LaunchedEffect(canScrollForward) {
        if (!canScrollForward && canScrollBackward) {
            component.sendIntent(
                CommentsComponent.Intent.LoadNextPage
            )
        }
    }

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isLoading,
        state = pullRefreshState,
        onRefresh = {
            component.sendIntent(
                CommentsComponent.Intent.Refresh
            )
        },
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(contentPadding.calculateTopPadding()),
                state = pullRefreshState,
                isRefreshing = isLoading,
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        LazyColumn(
            modifier = modifier.padding(end = defaultScrollbarPadding),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding,
        ) {
            items(
                items = comments,
                //key = CommentModelStable::commentID
            ) { comment ->
                VerticalSpacer(8.dp)
                CommentItem(
                    comment = comment,
                    hideAvatar = hideAvatar,
                    onFanficClick = { href ->
                        component.onOutput(
                            CommentsComponent.Output.OpenFanfic(href)
                        )
                    },
                    onUserClick = {
                        component.onOutput(
                            CommentsComponent.Output.OpenAuthor(comment.user.href)
                        )
                    },
                    onUrlClick = { url ->
                        component.onOutput(
                            CommentsComponent.Output.OpenUrl(url)
                        )
                    },
                    onLikeClick = {
                        component.sendIntent(
                            CommentsComponent.Intent.LikeComment(comment.commentID, !comment.isLiked)
                        )
                    },
                    onAddReply = {
                        onAddReply(
                            comment.user.name,
                            comment.blocks
                        )
                    },
                    onDelete = {
                        component.sendIntent(
                            CommentsComponent.Intent.DeleteComment(comment.commentID)
                        )
                    }
                )
            }
        }
        VerticalScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = defaultScrollbarPadding)
                .fillMaxHeight(),
            lazyListState = lazyListState
        )
    }
}

@Composable
private fun CommentItem(
    comment: CommentModelStable,
    hideAvatar: Boolean,
    onUserClick: () -> Unit,
    onFanficClick: (href: String) -> Unit,
    onUrlClick: (url: String) -> Unit,
    onLikeClick: () -> Unit,
    onAddReply: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val user = comment.user

    Row(
        modifier = Modifier
            .padding(horizontal = DefaultPadding.CardHorizontalPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        if (!hideAvatar) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onUserClick)
            )
            Spacer(modifier = Modifier.requiredWidth(9.dp))
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardDefaults.shape),
            shape = CardDefaults.shape
        ) {
            Row(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPaddingSmall)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.5F),
                )
                Text(
                    text = comment.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.5F),
                )
            }
            VerticalSpacer(3.dp)
            comment.blocks.forEach { block ->
                CommentBlockElement(
                    block = block,
                    onUrlClick = onUrlClick
                )
            }
            comment.forFanfic?.let { fanfic ->
                Spacer(modifier = Modifier.requiredHeight(2.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                VerticalSpacer(2.dp)
                Text(
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPaddingSmall)
                        .clickable { onFanficClick(fanfic.href) },
                    text = fanfic.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val iconTint by animateColorAsState(
                targetValue = if(comment.isLiked) likeColor else MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val buttonShape = MaterialTheme.shapes.medium
                val avatarShape = CircleShape
                val density = LocalDensity.current
                val layoutDirection = LocalLayoutDirection.current

                val avatarSize = 30.dp
                val avatarOffset = 12.dp
                val sizePx = with(density) { avatarSize.toPx() }
                val avatarOffsetPx = with(density) { 12.dp.toPx() }
                val outline = avatarShape.createOutline(
                    size = Size(sizePx, sizePx),
                    layoutDirection = layoutDirection,
                    density = density
                )

                TextButton(
                    onClick = onLikeClick,
                    enabled = !comment.isOwnComment,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = iconTint,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContentColor = iconTint
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp
                    ),
                    shape = buttonShape
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_like_outlined),
                        contentDescription = stringResource(Res.string.content_description_icon_like),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(18.dp),
                    )
                    HorizontalSpacer(8.dp)
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = comment.likes.toString()
                    )
                }
                if(comment.likedBy.isNotEmpty()) {
                    HorizontalSpacer(4.dp)
                    LazyRow(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        horizontalArrangement = Arrangement.spacedBy(-avatarOffset / 2)
                    ) {
                        itemsIndexed(comment.likedBy) { index, author ->
                            AsyncImage(
                                model = author.user.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(avatarSize)
                                    .clip(avatarShape)
                                    .thenIf(index != comment.likedBy.lastIndex) {
                                        drawWithLayer {
                                            drawContent()
                                            translate(
                                                left = drawContext.size.width - avatarOffsetPx
                                            ) {
                                                drawOutline(
                                                    outline = outline,
                                                    color = Color.Transparent,
                                                    blendMode = BlendMode.Clear
                                                )
                                            }
                                        }
                                    }

                            )
                        }
                    }
                }
                HorizontalSpacer(8.dp)
                TextButton(
                    onClick = onAddReply,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 8.dp
                    ),
                    shape = buttonShape
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_reply),
                        contentDescription = stringResource(Res.string.content_description_icon_reply),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(18.dp),
                    )
                    HorizontalSpacer(8.dp)
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = stringResource(Res.string.reply)
                    )
                }
                if(comment.isOwnComment) {
                    HorizontalSpacer(8.dp)
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 8.dp
                        ),
                        shape = buttonShape
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = stringResource(Res.string.content_description_icon_delete),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(18.dp),
                        )
                        HorizontalSpacer(8.dp)
                        Text(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            text = stringResource(Res.string.delete)
                        )
                    }
                    HorizontalSpacer(8.dp)
                }
            }
        }
    }
}

@Composable
private fun CommentBlockElement(
    modifier: Modifier = Modifier,
    block: CommentBlockModelStable,
    onUrlClick: (url: String) -> Unit
) {
    val quoteContainerColor = MaterialTheme.colorScheme.run {
        primary.compositeOver(surface).copy(0.7F)
    }
    val contentColor = MaterialTheme.colorScheme.onPrimary

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPaddingSmall)
                .fillMaxWidth()
                .background(
                    color = quoteContainerColor,
                    shape = CardDefaults.shape
                ),
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                block.quote?.let {
                    QuoteElement(
                        quote = it,
                        onUrlClick = onUrlClick
                    )
                    Spacer(modifier = Modifier.requiredHeight(7.dp))
                }
            }
        }
        HyperlinkText(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall),
            fullText = block.text,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            onLinkClick = onUrlClick
        )
    }
}

@Composable
private fun QuoteElement(
    quote: QuoteModelStable,
    onUrlClick: (url: String) -> Unit
) {
    val contentColor = LocalContentColor.current
    Column(
        modifier = Modifier.padding(start = 12.dp),
    ) {
        if (quote.userName.isNotEmpty()) {
            Text(
                text = quote.userName,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
            Spacer(modifier = Modifier.requiredHeight(2.dp))
        }
        if (quote.quote != null) {
            QuoteElement(
                quote = quote.quote!!,
                onUrlClick = onUrlClick
            )
            Spacer(modifier = Modifier.requiredHeight(2.dp))
        }
        if (quote.text.isNotEmpty()) {
            Spacer(modifier = Modifier.requiredHeight(2.dp))
            SubcomposeLayout { constraints ->
                val divider = subcompose(QUOTE_TEXT_SLOTS.DIVIDER) {
                    VerticalDivider(
                        color = contentColor,
                        thickness = 2.dp,
                    )
                }
                val text = subcompose(QUOTE_TEXT_SLOTS.TEXT) {
                    HyperlinkText(
                        modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall),
                        fullText = quote.text,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = contentColor
                        ),
                        onLinkClick = onUrlClick
                    )
                }

                val dividerWidth = divider.fastMaxOfOrNull { it.maxIntrinsicWidth(constraints.maxHeight) } ?: 0
                val textOffset = dividerWidth + 8

                val textPlaceables = text.fastMap {
                    it.measure(
                        constraints.copy(
                            maxWidth = constraints.maxWidth - textOffset,
                            minWidth = 0
                        )
                    )
                }
                val textHeight = textPlaceables.fastMaxOfOrNull { it.height } ?: 0

                val dividerPlaceable = divider.fastMap {
                    it.measure(
                        Constraints.fixedHeight(
                            height = textHeight
                        )
                    )
                }

                layout(
                    width = constraints.maxWidth,
                    height = textHeight
                ) {
                    dividerPlaceable.fastForEach {
                        it.placeRelative(0, 0)
                    }
                    textPlaceables.fastForEach {
                        it.placeRelative(textOffset, 0)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentsScreenContent(
    component: CommentsComponent,
    hideAvatar: Boolean = false,
) {
    val hazeState = remember { HazeState() }
    val blurEnabled = LocalBlurState.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(CommentsComponent.Output.NavigateBack)
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.toolbar_title_comments)),
                containerColor = if(blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(blurEnabled) {
                    hazeChild(state = hazeState)
                }
            )
        }
    ) { padding ->
        CommentsContent(
            component = component,
            hideAvatar = hideAvatar,
            contentPadding = padding,
            modifier = Modifier.thenIf(blurEnabled) {
                haze(state = hazeState)
            },
        )
    }
}

@Composable
fun PartCommentsContent(component: ExtendedCommentsComponent) {
    val hazeState = remember { HazeState() }
    val blurEnabled = LocalBlurState.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(CommentsComponent.Output.NavigateBack)
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.toolbar_title_chapter_comments)),
                containerColor = if(blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(blurEnabled) {
                    hazeChild(state = hazeState)
                },
            )
        },
        bottomBar = {
            WriteCommentContent(
                component = component.writeCommentComponent,
                containerColor = if(blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.thenIf(blurEnabled) {
                    hazeChild(state = hazeState)
                }
            )
        }
    ) { padding ->
        CommentsContent(
            component = component,
            modifier = Modifier.thenIf(blurEnabled) {
                haze(state = hazeState)
            },
            contentPadding = padding,
            onAddReply = { userName, blocks ->
                component.sendIntent(
                    CommentsComponent.Intent.AddReply(userName, blocks)
                )
            }
        )
    }
}

@Composable
private fun WriteCommentContent(
    component: WriteCommentComponent,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()

    val previewVisible by remember {
        derivedStateOf {
            state.text.isNotEmpty() &&
            state.renderedBlocks.any { it.quote != null }
        }
    }

    Column(
        modifier = modifier.background(
            color = containerColor
        )
    ) {
        AnimatedVisibility(
            visible = previewVisible,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                LazyColumn {
                    items(state.renderedBlocks) { block ->
                        CommentBlockElement(
                            block = block,
                            onUrlClick = {}
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1F, false),
        ) {
            TextField(
                value = state.text,
                onValueChange = component::editText,
                singleLine = false,
                maxLines = 6,
                shape = RectangleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                placeholder = {
                    Text(text = stringResource(Res.string.comments_textfield_placeholder))
                },
                modifier = Modifier.weight(1F),
            )
            Spacer(modifier = Modifier.width(4.dp))
            AnimatedVisibility(
                visible = state.text.isNotEmpty(),
                enter = fadeIn(spring()),
                exit = fadeOut(spring()),
            ) {
                IconButton(
                    onClick = component::post
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_send),
                        contentDescription = stringResource(Res.string.content_description_icon_send),
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
    }
}

@Suppress("ClassName")
private enum class QUOTE_TEXT_SLOTS {
    DIVIDER,
    TEXT
}