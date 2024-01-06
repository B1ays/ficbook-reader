package ru.blays.ficbookReader.components.authorProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.navigationBars
import io.github.skeptick.libres.compose.painterResource
import ru.blays.ficbookReader.components.commentsContent.CommentsContent
import ru.blays.ficbookReader.components.fanficsList.FanficsListContent
import ru.blays.ficbookReader.shared.data.dto.AuthorMainInfoStable
import ru.blays.ficbookReader.shared.data.dto.BlogPostCardModelStable
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.*
import ru.blays.ficbookReader.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbookReader.ui_components.HyperlinkText.HyperlinkText
import ru.blays.ficbookReader.ui_components.decomposePager.Pages
import ru.blays.ficbookReader.utils.LocalStackAnimator
import ru.blays.ficbookReader.utils.surfaceColorAtAlpha
import ru.blays.ficbookReader.values.DefaultPadding
import ru.blays.ficbookReader.values.Zero
import kotlin.math.roundToInt

@Composable
fun AuthorProfileContent(component: AuthorProfileComponent) {
    BoxWithConstraints {
        if(maxWidth > 500.dp) {
            LandscapeContent(component)
        } else {
            PortraitContent(component)
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun LandscapeContent(component: AuthorProfileComponent) {
    val state by component.state.subscribeAsState()
    val tabsState = component.tabs
    val profile = state.profile

    val onPageSelected = { page: Int ->
        component.sendIntent(
            AuthorProfileComponent.Intent.SelectTabs(
                index = page
            )
        )
    }

    if(profile != null) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .fillMaxWidth()
        ) {
            ProfileHeader(
                mainInfo = profile.authorMain,
                landscape = true,
                onBack = {
                    component.onOutput(
                        AuthorProfileComponent.Output.NavigateBack
                    )
                }
            )
            Row {
                RailTabs(
                    modifier = Modifier,
                    tabsState = tabsState,
                    onPageSelected = onPageSelected
                )
                ProfilePager(
                    state = tabsState,
                    onPageSelected = onPageSelected
                )
            }
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun PortraitContent(component: AuthorProfileComponent) {
    val state by component.state.subscribeAsState()
    val tabsState = component.tabs
    val profile = state.profile

    val onPageSelected = { page: Int ->
        component.sendIntent(
            AuthorProfileComponent.Intent.SelectTabs(
                index = page
            )
        )
    }

    if(profile != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            ProfileHeader(
                mainInfo = profile.authorMain,
                landscape = false,
                onBack = {
                    component.onOutput(
                        AuthorProfileComponent.Output.NavigateBack
                    )
                }
            )
            ChipTabs(
                tabsState = tabsState,
                onPageSelected = onPageSelected
            )
            ProfilePager(
                state = tabsState,
                onPageSelected = onPageSelected
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    mainInfo: AuthorMainInfoStable,
    landscape: Boolean,
    onBack: () -> Unit
) {
    val avatarSize = if(landscape) 125.dp else 100.dp
    val avatarSizePx = with(LocalDensity.current) {
        avatarSize.roundToPx()
    }

    SubcomposeLayout { constraints ->
        val headerImage = subcompose(
            slotId = ProfileHeaderSlots.HEADER_IMAGE
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = mainInfo.profileCoverUrl,
                    contentDescription = "Фон профиля",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier.matchParentSize()
                )
                CustomIconButton(
                    onClick = onBack,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    minSize = 28.dp,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        painter = painterResource(Res.image.ic_arrow_back),
                        contentDescription = "Иконка назад"
                    )
                }
            }
        }
        val avatar = subcompose(
            slotId = ProfileHeaderSlots.AVATAR
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .padding(5.dp)
            ) {
                AsyncImage(
                    model = mainInfo.avatarUrl,
                    contentDescription = "Аватарка автора",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }
        }
        val info = subcompose(
            slotId = ProfileHeaderSlots.INFO
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.large.copy(
                            topStart = CornerSize(0.dp),
                            topEnd = CornerSize(0.dp)
                        )
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            DefaultPadding.CardDefaultPadding
                        )
                        .fillMaxWidth(0.5F)
                ) {
                    Text(
                        text = mainInfo.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.requiredHeight(6.dp))
                    Text(
                        text = "Подписчиков: ${mainInfo.subscribers}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.8F
                        )
                    )
                }
            }
        }

        val headerPlaceable = headerImage.map {
            it.measure(
                constraints
            )
        }
        val headerHeight = headerPlaceable.fold(0) { acc, placeable ->
            maxOf(acc, placeable.height)
        }
        val infoPlaceable = info.map {
            it.measure(
                constraints
            )
        }
        val infoHeight = infoPlaceable.fold(0) { acc, placeable ->
            maxOf(acc, placeable.height)
        }
        val avatarResizedSize = avatarSizePx.coerceAtMost((infoHeight*1.5).roundToInt())
        val avatarPlaceable = avatar.map {
            it.measure(
                Constraints.fixed(
                    width = avatarResizedSize,
                    height = avatarResizedSize
                )
            )
        }

        layout(
            width = constraints.maxWidth,
            height = headerHeight + infoHeight
        ) {
            headerPlaceable.forEach {
                it.place(0, 0)
            }

            infoPlaceable.first().place(0, headerHeight)

            avatarPlaceable.first().place(
                x = ((constraints.maxWidth - avatarResizedSize)*0.8F).toInt(),
                y = (headerHeight + infoHeight)/2 - avatarResizedSize/2
            )
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun ProfilePager(
    state: Value<ChildPages<AuthorProfileComponent.TabConfig, AuthorProfileComponent.Tabs>>,
    onPageSelected: (index: Int) -> Unit
) {
    Pages(
        pages = state,
        onPageSelected = onPageSelected,
        userScrollEnabled = false
    ) { _, page ->
        when(page) {
            is AuthorProfileComponent.Tabs.Main -> ProfileMainInfo(page.component)
            is AuthorProfileComponent.Tabs.Blog -> BlogPostsRoot(page.component)
            is AuthorProfileComponent.Tabs.Presents -> PresentsContent(page.component)
            is AuthorProfileComponent.Tabs.Works -> FanficsListContent(page.component)
            is AuthorProfileComponent.Tabs.WorksAsBeta -> FanficsListContent(page.component)
            is AuthorProfileComponent.Tabs.WorksAsCoauthor -> FanficsListContent(page.component)
            is AuthorProfileComponent.Tabs.WorksAsGamma -> FanficsListContent(page.component)
            is AuthorProfileComponent.Tabs.Comments -> CommentsContent(
                component = page.component,
                hideAvatar = true
            )
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
private fun ChipTabs(
    tabsState: Value<ChildPages<AuthorProfileComponent.TabConfig, AuthorProfileComponent.Tabs>>,
    onPageSelected: (index: Int) -> Unit
) {
    val state by tabsState.subscribeAsState()
    LazyRow(
        modifier = Modifier
    ) {
        itemsIndexed(state.items) { index, tab ->
            InputChip(
                selected = state.selectedIndex == index,
                onClick = {
                    onPageSelected(index)
                },
                label = {
                    Text(
                        text = remember(tab) { getTitleForTab(tab.configuration) }
                    )
                },
                modifier = Modifier.padding(3.dp)
            )
        }
    }
}

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RailTabs(
    modifier: Modifier = Modifier,
    tabsState: Value<ChildPages<AuthorProfileComponent.TabConfig, AuthorProfileComponent.Tabs>>,
    onPageSelected: (index: Int) -> Unit
) {
    val state by tabsState.subscribeAsState()
    val scrollState = rememberScrollState()
    NavigationRail(
        header = null,
        windowInsets = WindowInsets.Zero,
        modifier = modifier
            .padding(vertical = 3.dp)
            .verticalScroll(scrollState)
    ) {
        state.items.forEachIndexed { index, tab ->
            NavigationRailItem(
                icon = {
                    val icon = getIconForTab(tab.configuration)
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    val title = getTitleForTab(tab.configuration)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                selected = state.selectedIndex == index,
                onClick = {
                    onPageSelected(index)
                }
            )
        }
    }
}

@Composable
private fun ProfileMainInfo(component: AuthorProfileComponent) {
    val state by component.state.subscribeAsState()
    val authorInfo = state.profile?.authorInfo
    val scrollState = rememberScrollState()
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (authorInfo != null) {
        val onUrlClick = { url: String ->
            component.onOutput(
                AuthorProfileComponent.Output.OpenUrl(url)
            )
        }

        SelectionContainer {
            Column(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPaddingSmall)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                if(authorInfo.about.isNotEmpty()) {
                    Text(
                        text = "О себе:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.requiredHeight(3.dp))
                    HyperlinkText(
                        fullText = authorInfo.about,
                        textStyle = textStyle,
                        onLinkClick = onUrlClick
                    )
                    Spacer(modifier = Modifier.requiredHeight(5.dp))
                }
                if(authorInfo.contacts.isNotEmpty()) {
                    Text(
                        text = "Контактная информация:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.requiredHeight(3.dp))
                    HyperlinkText(
                        fullText = authorInfo.contacts,
                        textStyle = textStyle,
                        onLinkClick = onUrlClick
                    )
                    Spacer(modifier = Modifier.requiredHeight(5.dp))
                }
                if(authorInfo.support.isNotEmpty()) {
                    Text(
                        text = "Поддержать автора:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Spacer(modifier = Modifier.requiredHeight(3.dp))
                    HyperlinkText(
                        fullText = authorInfo.support,
                        textStyle = textStyle,
                        onLinkClick = onUrlClick
                    )
                }
                Spacer(modifier = Modifier.requiredHeight(30.dp))
            }
        }
    }
}

@Composable
private fun BlogPostsRoot(
    component: AuthorBlogComponent
) {
    val animator = LocalStackAnimator.current
    Children(
        stack = component.childStack,
        animation = animator?.let { stackAnimation(it) },
        modifier = Modifier.fillMaxSize()
    ) {
        when(
            val instance = it.instance
        ) {
            is AuthorBlogComponent.Child.PostsList -> BlogPostsList(instance.component)
            is AuthorBlogComponent.Child.PostPage -> BlogPostPage(instance.component)
        }
    }
}

@Composable
private fun BlogPostsList(component: AuthorBlogPostsComponent) {
    val state by component.state.subscribeAsState()
    val posts = state.posts
    val lazyListState = rememberLazyListState()
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.4F),
                color = MaterialTheme.colorScheme.primary
            )
        } else if (state.error) {
            Column(
                modifier = Modifier.align(Alignment.Center)
            ) {
                Icon(
                    painter = painterResource(Res.image.ic_sad),
                    contentDescription = "Иконка грустный смайлик",
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier.fillMaxWidth(0.4F)
                )
                Spacer(modifier = Modifier.requiredHeight(10.dp))
                Text(
                    text = "Что-то пошло не так",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.surfaceTint
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier,
                state = lazyListState
            ) {
                items(posts) { post ->
                    BlogPostCard(
                        modifier = Modifier.animateItemPlacement(),
                        post = post,
                        onPostClicked = {
                            component.onOutput(
                                AuthorBlogPostsComponent.Output.OpenPostPage(
                                    postID = post.id
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlogPostPage(
    component: AuthorBlogPageComponent
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val state by component.state.subscribeAsState()
        val post = state.post
        val scrollState = rememberScrollState()
        if (state.loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.4F),
                color = MaterialTheme.colorScheme.surfaceTint
            )
        } else if (state.error) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(Res.image.ic_sad),
                    contentDescription = "Иконка грустный смайлик",
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier.fillMaxWidth(0.4F)
                )
                Spacer(modifier = Modifier.requiredHeight(10.dp))
                Text(
                    text = "Что-то пошло не так",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.surfaceTint
                )
            }
        } else if (post != null) {
            Column(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPadding)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomIconButton(
                        onClick = {
                            component.onOutput(
                                AuthorBlogPageComponent.Output.NavigateBack
                            )
                        },
                        minSize = 20.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_cancel),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(0.8F)
                    ) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Spacer(modifier = Modifier.requiredHeight(4.dp))
                        Text(
                            text = post.date,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.requiredHeight(10.dp))
                HyperlinkText(
                    fullText = post.text,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    onLinkClick = {
                        component.onOutput(
                            AuthorBlogPageComponent.Output.OpenUrl(
                                url = it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.requiredHeight(30.dp))
            }
        }
    }
}

@Composable
private fun BlogPostCard(
    modifier: Modifier = Modifier,
    post: BlogPostCardModelStable,
    onPostClicked: () -> Unit
) {
    val shape = CardDefaults.shape
    val foregroundCardColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.6F)
    val foregroundCardContentColor = MaterialTheme.colorScheme.onSurface
    Card(
        modifier = modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = foregroundCardColor,
            contentColor = foregroundCardContentColor
        ),
        onClick = onPostClicked
    ) {
        Column(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPadding)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.requiredHeight(4.dp))
            Text(
                text = post.date,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if(post.text.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                onClick = onPostClicked
            ) {
                Text(
                    text = post.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 9,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(DefaultPadding.CardDefaultPadding)
                )
            }
        }
    }
}

@Composable
fun PresentsContent(component: AuthorPresentsComponent) {
    // TODO
}

private fun getTitleForTab(tab: AuthorProfileComponent.TabConfig): String {
    return when(tab) {
        is AuthorProfileComponent.TabConfig.Main -> "Инфо"
        is AuthorProfileComponent.TabConfig.Blog -> "Блог"
        is AuthorProfileComponent.TabConfig.Presents -> "Подарки"
        is AuthorProfileComponent.TabConfig.Works -> "Работы"
        is AuthorProfileComponent.TabConfig.WorksAsBeta -> "Бета"
        is AuthorProfileComponent.TabConfig.WorksAsCoauthor -> "Соавтор"
        is AuthorProfileComponent.TabConfig.WorksAsGamma -> "Гамма"
        is AuthorProfileComponent.TabConfig.Comments -> "Отзывы"
    }
}

@Composable
private fun getIconForTab(tab: AuthorProfileComponent.TabConfig): Painter {
    return when(tab) {
        is AuthorProfileComponent.TabConfig.Main -> painterResource(Res.image.ic_user)
        is AuthorProfileComponent.TabConfig.Blog -> painterResource(Res.image.ic_blog)
        is AuthorProfileComponent.TabConfig.Presents -> painterResource(Res.image.ic_present)
        is AuthorProfileComponent.TabConfig.Works -> painterResource(Res.image.ic_book)
        is AuthorProfileComponent.TabConfig.WorksAsBeta -> painterResource(Res.image.ic_book)
        is AuthorProfileComponent.TabConfig.WorksAsCoauthor -> painterResource(Res.image.ic_book)
        is AuthorProfileComponent.TabConfig.WorksAsGamma -> painterResource(Res.image.ic_book)
        is AuthorProfileComponent.TabConfig.Comments -> painterResource(Res.image.ic_comment)
    }
}

private enum class ProfileHeaderSlots {
    HEADER_IMAGE,
    AVATAR,
    INFO;
}