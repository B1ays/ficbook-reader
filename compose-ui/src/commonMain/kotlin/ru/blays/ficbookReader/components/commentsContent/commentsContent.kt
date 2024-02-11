package ru.blays.ficbookReader.components.commentsContent

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.example.myapplication.compose.Res
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.github.skeptick.libres.compose.painterResource
import ru.blays.ficbookReader.shared.data.dto.CommentBlockModelStable
import ru.blays.ficbookReader.shared.data.dto.CommentModelStable
import ru.blays.ficbookReader.shared.data.dto.QuoteModelStable
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.ExtendedCommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.WriteCommentComponent
import ru.blays.ficbookReader.ui_components.HyperlinkText.HyperlinkText
import ru.blays.ficbookReader.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbookReader.utils.LocalGlassEffectConfig
import ru.blays.ficbookReader.utils.thenIf
import ru.blays.ficbookReader.values.DefaultPadding
import ru.blays.ficbookReader.values.defaultScrollbarPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun CommentsContent(
    component: CommentsComponent,
    contentPadding: PaddingValues? = null,
    hideAvatar: Boolean = false,
    modifier: Modifier = Modifier,
    onAddReply: (userName: String, blocks: List<CommentBlockModelStable>) -> Unit = { userName, block -> }
) {
    val state by component.state.subscribeAsState()
    val comments = state.comments

    val lazyListState = rememberLazyListState()

    val blurConfig = LocalGlassEffectConfig.current

    val canScrollForward = lazyListState.canScrollForward
    val canScrollBackward = lazyListState.canScrollBackward

    LaunchedEffect(canScrollForward) {
        if(!canScrollForward && canScrollBackward) {
            component.sendIntent(
                CommentsComponent.Intent.LoadNextPage
            )
        }
    }

    Box(
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = defaultScrollbarPadding),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = contentPadding ?: PaddingValues(0.dp),
        ) {
            items(comments) { comment ->
                Spacer(modifier = Modifier.height(7.dp))
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
                .padding(end = 4.dp)
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
    onAddReply: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val user = comment.user

    val density = LocalDensity.current

    var menuExpanded by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }

    Row(
        modifier = Modifier
            .padding(horizontal = DefaultPadding.CardHorizontalPadding)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        if(!hideAvatar) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "Аватарка автора комментария",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onUserClick)
            )
            Spacer(modifier = Modifier.requiredWidth(9.dp))
        }
        val cardInteractionSource = remember { MutableInteractionSource() }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardDefaults.shape)
                .indication(
                    interactionSource = cardInteractionSource,
                    indication = LocalIndication.current
                )
                .hoverable(
                    interactionSource = cardInteractionSource,
                    enabled = true
                )
                .pointerInput(true) {
                    detectTapGestures(
                        onPress = {
                            val press = PressInteraction.Press(it)
                            cardInteractionSource.emit(press)
                            awaitRelease()
                            cardInteractionSource.emit(PressInteraction.Release(press))
                        },
                        onLongPress = {
                            menuOffset = with(density) {
                                DpOffset(
                                    x = it.x.toDp(),
                                    y = 0.dp
                                )
                            }
                            menuExpanded = true
                        }
                    )
                },
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
            Spacer(modifier = Modifier.requiredHeight(3.dp))
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
                Spacer(modifier = Modifier.requiredHeight(2.dp))
                ClickableText(
                    modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall),
                    onClick = {
                        onFanficClick(fanfic.href)
                    },
                    text = AnnotatedString(fanfic.name),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                offset = menuOffset,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.image.ic_reply),
                            contentDescription = "Иконка ответ",
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    text = {
                        Text("Ответить")
                    },
                    onClick = onAddReply
                )
                if(comment.isOwnComment) {
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.image.ic_delete),
                                contentDescription = "Иконка удалить",
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        text = {
                            Text("Удалить")
                        },
                        onClick = onDelete
                    )
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
        if(quote.userName.isNotEmpty()) {
            Text(
                text = quote.userName,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor
            )
            Spacer(modifier = Modifier.requiredHeight(2.dp))
        }
        if(quote.quote != null) {
            QuoteElement(
                quote = quote.quote!!,
                onUrlClick = onUrlClick
                )
            Spacer(modifier = Modifier.requiredHeight(2.dp))
        }
        if(quote.text.isNotEmpty()) {
            Spacer(modifier = Modifier.requiredHeight(2.dp))
            SubcomposeLayout { constraints ->
                val divider = subcompose(QUOTE_TEXT_SLOTS.DIVIDER) {
                    VerticalDivider(
                        color = contentColor,
                        thickness = 2.dp,
                        cap = StrokeCap.Round
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
                val textHeight = text.fold(0) { acc, it ->
                    maxOf(acc, it.maxIntrinsicHeight(constraints.maxWidth))
                }
                val dividerPlaceable = divider.map {
                    it.measure(
                        Constraints.fixedHeight(
                            height = textHeight
                        )
                    )
                }
                val dividerWidth = dividerPlaceable.fold(0) { acc, placeable ->
                    maxOf(acc, placeable.width)
                }
                val textOffset = dividerWidth+8
                val textResizedPlaceable = text.map {
                    it.measure(
                        constraints.copy(
                            maxWidth = constraints.maxWidth-textOffset
                        )
                    )
                }

                layout(
                    width = constraints.maxWidth,
                    height = textHeight
                ) {
                    dividerPlaceable.forEach {
                        it.place(0, 0)
                    }
                    textResizedPlaceable.forEach {
                        it.place(textOffset, 0)
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
    val glassEffectConfig = LocalGlassEffectConfig.current
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
                            painter = painterResource(Res.image.ic_arrow_back),
                            contentDescription = "Стрелка назад"
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large("Отзывы"),
                containerColor = if(glassEffectConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )
                }
            )
        }
    ) { padding ->
        CommentsContent(
            component = component,
            hideAvatar = hideAvatar,
            contentPadding = padding,
            modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                haze(state = hazeState)
            },
        )
    }
}

@Composable
fun PartCommentsContent(component: ExtendedCommentsComponent) {
    val hazeState = remember { HazeState() }
    val glassEffectConfig = LocalGlassEffectConfig.current
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
                            painter = painterResource(Res.image.ic_arrow_back),
                            contentDescription = "Стрелка назад"
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large("Отзывы к главе"),
                containerColor = if(glassEffectConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(glassEffectConfig.blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )
                },
            )
        },
        bottomBar = {
            WriteCommentContent(
                component = component.writeCommentComponent,
                containerColor = if(glassEffectConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = glassEffectConfig.style
                    )
                }
            )
        }
    ) { padding ->
        CommentsContent(
            component = component,
            modifier = Modifier.thenIf(glassEffectConfig.blurEnabled) {
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

    Column(
        modifier = modifier.background(
            color = containerColor
        )
    ) {
        AnimatedVisibility(
            visible = state.text.isNotEmpty() &&
                state.renderedBlocks.isNotEmpty() &&
                state.renderedBlocks.any { it.quote != null },
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
                    Text("Оставьте свой отзыв")
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
                        painter = painterResource(Res.image.ic_send),
                        contentDescription = "Иконка отправки",
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Suppress("ClassName")
private enum class QUOTE_TEXT_SLOTS {
    DIVIDER,
    TEXT
}