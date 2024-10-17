package ru.blays.ficbook.components.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.components.collectionContent.CollectionsContentExtended
import ru.blays.ficbook.platformUtils.WindowSize
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration.MainScreenComponent
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery
import ru.blays.ficbook.reader.shared.data.sections.UserSectionsStable
import ru.blays.ficbook.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.CornerSmoothing
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.SquircleShape
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.utils.LocalBlurState
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(component: MainScreenComponent) {
    val windowSize = WindowSize()

    val tabs = component.tabs
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {
            tabs.size
        }
    )

    if (windowSize.width > 600) {
        LandscapeContent(
            component = component,
            pagerState = pagerState
        )
    } else {
        PortraitContent(
            component = component,
            drawerState = drawerState,
            pagerState = pagerState
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LandscapeContent(
    component: MainScreenComponent,
    pagerState: PagerState
) {
    val tabs = component.tabs
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
    ) {
        DrawerLandscape(
            component = component,
            modifier = Modifier
                .widthIn(
                    min = 250.dp,
                    max = 1100.dp
                )
                .fillMaxWidth(0.3F)
        )
        Column {
            PagerChips(tabs, pagerState)
            PagerContent(component, pagerState)
        }
    }
}

@Composable
private fun PortraitContent(
    component: MainScreenComponent,
    drawerState: DrawerState,
    pagerState: PagerState
) {
    val tabs = component.tabs
    val scope = rememberCoroutineScope()

    val hazeState = remember { HazeState() }
    val blurEnabled = LocalBlurState.current

    val containerColor = if(blurEnabled) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.surface
    }

    ModalNavigationDrawer(
        drawerContent = {
            DrawerPortrait(component = component)
        },
        drawerState = drawerState,
        modifier = Modifier
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier.thenIf(blurEnabled) {
                        hazeChild(state = hazeState)
                    }
                ) {
                    CollapsingToolbar(
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    contentDescription = stringResource(Res.string.content_description_icon_menu)
                                )
                            }
                        },
                        actions = {
                            CustomIconButton(
                                onClick = {
                                    component.onOutput(MainScreenComponent.Output.Search)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(2.dp),
                                shape = CircleShape,
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_search),
                                    contentDescription = stringResource(Res.string.content_description_icon_search),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            HorizontalSpacer(5.dp)
                            UserIconButton(
                                component = component,
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(42.dp),
                            )
                        },
                        collapsingTitle = CollapsingTitle.large(stringResource(Res.string.app_name)),
                        insets = WindowInsets.statusBars,
                        containerColor = containerColor,
                        collapsedElevation = if(blurEnabled) 0.dp else 4.dp,
                    )
                    PagerChips(
                        tabs = tabs,
                        pagerState = pagerState,
                        modifier = Modifier.background(containerColor),
                    )
                }
            }
        ) { padding ->
            PagerContent(
                component = component,
                pagerState = pagerState,
                contentPadding = padding,
                modifier = Modifier.thenIf(blurEnabled) {
                    haze(hazeState)
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerChips(
    tabs: Array<MainScreenComponent.TabModel>,
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val currentTab = pagerState.currentPage

    LazyRow(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth(),
    ) {
        itemsIndexed(tabs) { index, value ->
            InputChip(
                modifier = Modifier.padding(horizontal = 3.dp),
                selected = currentTab == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                label = { Text(text = value.name) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerContent(
    component: MainScreenComponent,
    pagerState: PagerState,
    contentPadding: PaddingValues? = null,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        modifier = modifier.fillMaxWidth(),
        state = pagerState,
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> {
                MainFeedContent(
                    component.feedComponent,
                    contentPadding
                )
            }
            1 -> {
                PopularCategoriesContent(
                    component.popularSectionsComponent,
                    contentPadding
                )
            }
            2 -> {
                CollectionsContentExtended(
                    component.collectionsComponent,
                    contentPadding
                )
            }
            3 -> {
                SavedFanficsContent(
                    component.savedFanficsComponent,
                    contentPadding
                )
            }
        }
    }
}

@Composable
private fun DrawerLandscape(
    modifier: Modifier = Modifier,
    component: MainScreenComponent
) {
    val currentUser by component.state.collectAsState()
    val scrollState = rememberScrollState()
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserIconButton(
                component = component,
                modifier = Modifier
                    .padding(3.dp)
                    .size(50.dp),
            )
            currentUser?.let {
                HorizontalSpacer(5.dp)
                Text(
                    text = it.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.weight(1F))
            CustomIconButton(
                onClick = {
                    component.onOutput(
                        MainScreenComponent.Output.Search
                    )
                },
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = stringResource(Res.string.content_description_icon_search),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        DrawerContent(component = component)
    }
}

@Composable
private fun DrawerPortrait(
    modifier: Modifier = Modifier,
    component: MainScreenComponent
) {
    val scrollState = rememberScrollState()
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(scrollState)
    ) {
        DrawerContent(component = component)
    }
}

@Composable
private fun DrawerContent(
    component: MainScreenComponent
) {
    val userSections = UserSectionsStable.default

    fun navigateToSection(section: SectionWithQuery) {
        component.onOutput(
            MainScreenComponent.Output.OpenFanficsList(section)
        )
    }

    Column(
        modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall)
    ) {
        Text(
            text = stringResource(Res.string.mainScreen_drawer_category_personal),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineSmall
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        val favouritesSection = userSections.favourites
        NavigationDrawerItem(
            label = {
                Text(text = favouritesSection.name)
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_star_filled),
                    contentDescription = stringResource(Res.string.content_description_icon_star)
                )
            },
            selected = false,
            onClick = {
                navigateToSection(favouritesSection)
            }
        )
        val likedSection = userSections.liked
        NavigationDrawerItem(
            label = {
                Text(text = likedSection.name)
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_like_filled),
                    contentDescription = stringResource(Res.string.content_description_icon_like)
                )
            },
            selected = false,
            onClick = {
                navigateToSection(likedSection)
            }
        )
        val readedSection = userSections.readed
        NavigationDrawerItem(
            label = {
                Text(text = readedSection.name)
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_bookmark_filled),
                    contentDescription = stringResource(Res.string.content_description_icon_bookmark)
                )
            },
            selected = false,
            onClick = {
                navigateToSection(readedSection)
            }
        )
        val followSection = userSections.follow
        NavigationDrawerItem(
            label = {
                Text(text = followSection.name)
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_star_filled),
                    contentDescription = stringResource(Res.string.content_description_icon_star)
                )
            },
            selected = false,
            onClick = {
                navigateToSection(followSection)
            }
        )
        val visitedSection = userSections.visited
        NavigationDrawerItem(
            label = {
                Text(text = visitedSection.name)
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_eye_filled),
                    contentDescription = stringResource(Res.string.content_description_icon_eye)
                )
            },
            selected = false,
            onClick = {
                navigateToSection(visitedSection)
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(Res.string.mainScreen_drawer_category_standart),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineSmall
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        NavigationDrawerItem(
            label = {
                Text(text = stringResource(Res.string.authors))
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_users),
                    contentDescription = stringResource(Res.string.content_description_icon_users)
                )
            },
            selected = false,
            onClick = {
                component.onOutput(
                    MainScreenComponent.Output.OpenUsersScreen
                )
            }
        )
        NavigationDrawerItem(
            label = {
                Text(text = stringResource(Res.string.notifications))
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_bell),
                    contentDescription = stringResource(Res.string.content_description_icon_bell)
                )
            },
            selected = false,
            onClick = {
                component.onOutput(
                    MainScreenComponent.Output.OpenNotifications
                )
            }
        )
        NavigationDrawerItem(
            label = {
                Text(text = stringResource(Res.string.random_fanfic))
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_dice),
                    contentDescription = stringResource(Res.string.content_description_icon_dice)
                )
            },
            selected = false,
            onClick = {
                component.onOutput(
                    MainScreenComponent.Output.OpenRandomFanficPage
                )
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        NavigationDrawerItem(
            label = {
                Text(text = stringResource(Res.string.settings))
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_settings),
                    contentDescription = stringResource(Res.string.content_description_icon_settings)
                )
            },
            selected = false,
            onClick = {
                component.onOutput(
                    MainScreenComponent.Output.OpenSettings
                )
            }
        )
        NavigationDrawerItem(
            label = {
                Text(text = stringResource(Res.string.toolbar_title_about))
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.ic_info),
                    contentDescription = stringResource(Res.string.content_description_icon_info)
                )
            },
            selected = false,
            onClick = {
                component.onOutput(
                    MainScreenComponent.Output.OpenAbout
                )
            }
        )
    }
}

@Composable
fun UserIconButton(
    component: MainScreenComponent,
    shape: Shape = SquircleShape(cornerSmoothing = CornerSmoothing.High),
    modifier: Modifier = Modifier
) {
    val currentUser by component.state.collectAsState()

    AnimatedContent(
        targetState = currentUser,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        modifier = modifier
            .clip(shape)
            .clickable {
                component.onOutput(MainScreenComponent.Output.UserProfile)
            },
    ) { user ->
        if(user != null) {
            AsyncImage(
                model = File(user.avatarPath),
                contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_user),
                contentDescription = stringResource(Res.string.content_description_icon_author_avatar_stub),
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
                        shape = shape
                    ),
            )
        }
    }
}