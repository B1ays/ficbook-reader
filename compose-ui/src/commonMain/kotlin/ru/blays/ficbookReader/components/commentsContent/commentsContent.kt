package ru.blays.ficbookReader.components.commentsContent

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.systemBarsPadding
import io.github.skeptick.libres.compose.painterResource
import ru.blays.ficbookReader.shared.data.dto.CommentBlockModelStable
import ru.blays.ficbookReader.shared.data.dto.CommentModelStable
import ru.blays.ficbookReader.shared.data.dto.QuoteModelStable
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.ExtendedCommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.WriteCommentComponent
import ru.blays.ficbookReader.ui_components.LinkifyText.TextWithLinks
import ru.blays.ficbookReader.ui_components.Scrollbar.VerticalScrollbar
import ru.blays.ficbookReader.values.DefaultPadding
import ru.blays.ficbookReader.values.defaultScrollbarPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar

@Composable
fun CommentsContent(
    component: CommentsComponent,
    hideAvatar: Boolean = false,
    modifier: Modifier = Modifier,
    onAddReply: (userName: String, block: CommentBlockModelStable) -> Unit = {userName, block ->}
) {
    val state by component.state.subscribeAsState()
    val comments = state.comments

    val lazyListState = rememberLazyListState()

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
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = defaultScrollbarPadding),
            state = lazyListState,
            horizontalAlignment = Alignment.CenterHorizontally
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
                    onAddReply = onAddReply
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
    onAddReply: (userName: String, block: CommentBlockModelStable) -> Unit
) {
    val user = comment.user
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
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    onUrlClick = onUrlClick,
                    onReply = {
                        onAddReply(comment.user.name, block)
                    }
                )
            }
            if(comment.forFanfic != null) {
                Spacer(modifier = Modifier.requiredHeight(2.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.requiredHeight(2.dp))
                ClickableText(
                    modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall),
                    onClick = {
                        onFanficClick(comment.forFanfic!!.href)
                    },
                    text = AnnotatedString(comment.forFanfic!!.name),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CommentBlockElement(
    block: CommentBlockModelStable,
    onUrlClick: (url: String) -> Unit,
    onReply: () -> Unit
) {
    val quoteContainerColor = MaterialTheme.colorScheme.run {
        primary.compositeOver(surface).copy(0.7F)
    }
    val contentColor = MaterialTheme.colorScheme.onPrimary

    var menuExpanded by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = menuExpanded,
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
            onClick = onReply
        )
    }

    Column(
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = {
                menuExpanded = true
            }
        )
    ) {
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
        TextWithLinks(
            text = block.text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall),
            onUrlClick = onUrlClick
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
                    TextWithLinks(
                        text = quote.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = contentColor
                        ),
                        onUrlClick = onUrlClick
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
    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        topBar = {
            CollapsingsToolbar(
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
                collapsingTitle = CollapsingTitle.small("Отзывы")
            )
        }
    ) { padding ->
        CommentsContent(
            component = component,
            hideAvatar = hideAvatar,
            modifier = Modifier.padding(top = padding.calculateTopPadding())
        )
    }
}

@Composable
fun PartCommentsContent(component: ExtendedCommentsComponent) {
    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        topBar = {
            CollapsingsToolbar(
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
                collapsingTitle = CollapsingTitle.small("Отзывы к главе")
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(top = padding.calculateTopPadding())
        ) {
            CommentsContent(
                component = component,
                modifier = Modifier.weight(1F),
                onAddReply = { userName, block ->
                    component.sendIntent(
                        CommentsComponent.Intent.AddReply(userName, block)
                    )
                }
            )
            WriteCommentContent(component.writeCommentComponent)
        }
    }
}

@Composable
private fun WriteCommentContent(component: WriteCommentComponent) {
    val state by component.state.subscribeAsState()
    Column {
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
                            onUrlClick = {},
                            onReply = {}
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
                maxLines = 5,
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