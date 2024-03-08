package ru.blays.ficbook.components.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.moriatsushi.insetsx.systemBarsPadding
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.`compose-ui`.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.platformUtils.WindowSize
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery
import ru.blays.ficbook.reader.shared.data.sections.userSections
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration.MainScreenComponent
import ru.blays.ficbook.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbook.utils.LocalGlassEffectConfig
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
            .systemBarsPadding()
            .fillMaxWidth()
    ) {
        DrawerLandscape(
            component = component,
            modifier = Modifier.fillMaxWidth(0.3F)
        )
        Column {
            PagerChips(tabs, pagerState)
            PagerContent(component, pagerState)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
private fun PortraitContent(
    component: MainScreenComponent,
    drawerState: DrawerState,
    pagerState: PagerState
) {
    val tabs = component.tabs
    val scope = rememberCoroutineScope()

    val hazeState = remember { HazeState() }
    val blurConfig = LocalGlassEffectConfig.current

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
                    modifier = Modifier.thenIf(blurConfig.blurEnabled) {
                        hazeChild(
                            state = hazeState,
                            style = blurConfig.style
                        )
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
                            Spacer(modifier = Modifier.requiredWidth(5.dp))
                            UserIconButton(component)
                        },
                        collapsingTitle = CollapsingTitle.large(stringResource(Res.string.app_name)),
                        insets = WindowInsets.statusBars,
                        containerColor = if(blurConfig.blurEnabled) {
                            Color.Transparent
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        collapsedElevation = if(blurConfig.blurEnabled) 0.dp else 4.dp,
                    )
                    PagerChips(
                        tabs = tabs,
                        pagerState = pagerState
                    )
                }
            }
        ) { padding ->
            PagerContent(
                component = component,
                pagerState = pagerState,
                contentPadding = padding,
                modifier = Modifier.thenIf(blurConfig.blurEnabled) {
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

    var currentTab by rememberSaveable {
        mutableStateOf(pagerState.currentPage)
    }

    LazyRow(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
    ) {
        itemsIndexed(tabs) { index, value ->
            InputChip(
                modifier = Modifier
                    .padding(horizontal = 3.dp),
                selected = currentTab == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                    currentTab = index
                },
                label = {
                    Text(text = value.name)
                }
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
                CollectionsComponent(
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

@OptIn(ExperimentalResourceApi::class)
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
            UserIconButton(component)
            currentUser?.let {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = it.name,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
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

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun DrawerContent(
    component: MainScreenComponent
) {
    val userSections = userSections

    fun navigateToSection(section: SectionWithQuery) {
        component.onOutput(
            MainScreenComponent.Output.OpenFanficsList(section)
        )
    }

    Text(
        text = stringResource(Res.string.mainScreen_drawer_category_personal),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.headlineSmall
    )
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
    val favouritesSection = remember { userSections.favourites }
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
    val likedSection = remember { userSections.liked }
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
    val readedSection = remember { userSections.readed }
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
    val followSection = remember { userSections.follow }
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
    val visitedSection = remember { userSections.visited }
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
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun UserIconButton(
    component: MainScreenComponent
) {
    val currentUser by component.state.collectAsState()
    IconButton(
        onClick = {
            component.onOutput(MainScreenComponent.Output.UserProfile)
        }
    ) {
        currentUser?.let {
            AsyncImage(
                model = File(it.avatarPath),
                contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp)
            )
        } ?: Icon(
            modifier = Modifier
                .size(40.dp)
                .padding(2.dp),
            painter = painterResource(Res.drawable.ic_user),
            contentDescription = stringResource(Res.string.content_description_icon_author_avatar_stub)
        )
    }
}