package ru.blays.ficbookReader.components.main

import androidx.compose.animation.core.spring
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.systemBarsPadding
import io.github.skeptick.libres.compose.painterResource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.platformUtils.WindowSize
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookReader.shared.data.sections.userSections
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.MainScreenComponent
import ru.blays.ficbookReader.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar

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

    if(windowSize.width > 600) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PortraitContent(
    component: MainScreenComponent,
    drawerState: DrawerState,
    pagerState: PagerState
) {
    val tabs = component.tabs
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerContent = {
            DrawerPortrait(component = component)
        },
        drawerState = drawerState,
        modifier = Modifier.systemBarsPadding()
    ) {
        Scaffold(
            topBar = {
                CollapsingsToolbar(
                    actions = {
                        CustomIconButton(
                            onClick = {
                                // TODO Realize search menu
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .padding(2.dp),
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(
                                painter = painterResource(Res.image.ic_search),
                                contentDescription = "Иконка поиска",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.requiredWidth(5.dp))
                        UserIconButton(component)
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.animateTo(
                                        targetValue = DrawerValue.Open,
                                        anim = spring()
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "иконка меню"
                            )
                        }
                    },
                    collapsingTitle = CollapsingTitle.large("Ficbook Reader")
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = padding.calculateTopPadding())
            ) {
                PagerChips(tabs, pagerState)
                PagerContent(component, pagerState)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PagerChips(
    tabs: Array<MainScreenComponent.TabModel>,
    pagerState: PagerState
) {
    val scope = rememberCoroutineScope()

    var currentTab by rememberSaveable {
        mutableStateOf(pagerState.currentPage)
    }

    LazyRow(
        modifier = Modifier
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
    pagerState: PagerState
) {
    HorizontalPager(
        modifier = Modifier
            .fillMaxWidth(),
        state = pagerState,
        userScrollEnabled = false
    ) { page ->
        when (page) {
            0 -> {
                MainFeedContent(component.feedComponent)
            }
            1 -> {
                PopularCategoriesContent(component.popularSectionsComponent)
            }
            2 -> {
                CollectionsComponent(component.collectionsComponent)
            }
            3 -> {
                SavedFanficsContent(component.savedFanficsComponent)
            }
        }
    }
}

@Composable
private fun DrawerLandscape(
    modifier: Modifier = Modifier,
    component: MainScreenComponent
) {
    val state by component.state.subscribeAsState()
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
            state.user?.name?.let { userName ->
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = userName,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            CustomIconButton(
                onClick = {
                    // TODO Realize search menu
                },
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    painter = painterResource(Res.image.ic_search),
                    contentDescription = "Иконка поиска",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        DrawerContent(
            component = component,
            modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall)
        )
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
        DrawerContent(
            component = component,
            modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingSmall)
        )
    }
}

@Composable
private fun DrawerContent(
    component: MainScreenComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val isAuthorized = remember(state) { state.authorized }
    val userSections = userSections

    fun navigateToSection(section: SectionWithQuery) {
        component.onOutput(
            MainScreenComponent.Output.OpenFanficsList(section)
        )
    }

    Column(
        modifier = modifier
    ) {
        if(isAuthorized) {
            Text(
                text = "Личные",
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
                        painter =  painterResource(Res.image.ic_star_filled),
                        contentDescription = "Иконка звезда"
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
                        painter =  painterResource(Res.image.ic_like_filled),
                        contentDescription = "Иконка лайк"
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
                        painter =  painterResource(Res.image.ic_bookmark_filled),
                        contentDescription = "Иконка книга с закладкой"
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
                        painter = painterResource(Res.image.ic_star_filled),
                        contentDescription = "Иконка звезда"
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
                        painter =  painterResource(Res.image.ic_eye_filled),
                        contentDescription = "Иконка глаз"
                    )
                },
                selected = false,
                onClick = {
                    navigateToSection(visitedSection)
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = "Стандартные",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineSmall
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        NavigationDrawerItem(
            label = {
                Text(text = "Авторы")
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter =  painterResource(Res.image.ic_users),
                    contentDescription = "Иконка пользователи"
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
                Text(text = "Случайный фанфик")
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter =  painterResource(Res.image.ic_dice),
                    contentDescription = "Иконка игральные кости"
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
                Text(text = "Настройки")
            },
            icon = {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter =  painterResource(Res.image.ic_settings),
                    contentDescription = "Иконка настройки"
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
}

@Composable
fun UserIconButton(
    component: MainScreenComponent
) {
    val state by component.state.subscribeAsState()
    IconButton(
        onClick = {
            component.onOutput(MainScreenComponent.Output.UserProfile)
        }
    ) {
        if (state.authorized) {
            val resource = asyncPainterResource(state.user?.avatarUrl ?: "")
            KamelImage(
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp),
                contentDescription = "Иконка пользователя",
                resource = resource,
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                modifier = Modifier
                    .size(40.dp)
                    .padding(2.dp),
                painter = painterResource(Res.image.ic_user),
                contentDescription = "Заполнитель иконки пользователя"
            )
        }
    }
}