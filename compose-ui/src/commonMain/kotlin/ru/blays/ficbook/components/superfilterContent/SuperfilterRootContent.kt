package ru.blays.ficbook.components.superfilterContent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.pages.ChildPages
import com.arkivanov.decompose.extensions.compose.pages.PagesScrollAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.platformUtils.WindowSize
import ru.blays.ficbook.platformUtils.scaleContent
import ru.blays.ficbook.reader.shared.components.superfilterComponents.SuperfilterComponent
import ru.blays.ficbook.reader.shared.components.superfilterComponents.SuperfilterTabComponent
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.SquircleShape
import ru.blays.ficbook.ui_components.dialogComponents.DialogPlatform
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.LocalGlassEffectConfig
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun SuperfilterRootContent(component: SuperfilterComponent) {
    val pages by component.pages.subscribeAsState()
    val currentPage = with(pages) { items[selectedIndex] }

    val windowSize = WindowSize()
    val widthFraction = if(scaleContent) {
        when (windowSize.width) {
            in 1300..Int.MAX_VALUE -> 0.6F
            in 1000..1300 -> 0.7F
            in 800..1000 -> 0.8F
            in 600..800 -> 0.9F
            else -> 1F
        }
    } else 1F

    Scaffold(
        topBar = {
            Column {
                CollapsingToolbar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                component.onOutput(
                                    SuperfilterComponent.Output.NavigateBack
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = stringResource(Res.string.content_description_icon_back)
                            )
                        }
                    },
                    collapsingTitle = CollapsingTitle.small(
                        stringResource(Res.string.toolbar_title_superfilter)
                    ),
                    containerColor = MaterialTheme.colorScheme.surface,
                    collapsedElevation = 4.dp,
                    insets = WindowInsets.statusBars
                )
                TabsRow(
                    selectedTab = pages.selectedIndex,
                    onTabSelected = component::changeTab
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    currentPage.instance?.onIntent(
                        SuperfilterTabComponent.Intent.ShowAddDialog
                    )
                },
                shape = SquircleShape(),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_plus),
                    contentDescription = stringResource(Res.string.content_description_icon_plus),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    ) { padding ->
        ChildPages(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .padding(padding),
            pages = pages,
            onPageSelected = component::changeTab,
            scrollAnimation = PagesScrollAnimation.Default
        ) { _, page ->
            Page(page)
        }
    }
}

@Composable
private fun TabsRow(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier.horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalSpacer(8.dp)
        InputChip(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            label = {
                Text(text = stringResource(Res.string.superfilter_category_authors))
            }
        )
        HorizontalSpacer(8.dp)
        InputChip(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            label = {
                Text(text = stringResource(Res.string.superfilter_category_fanfics))
            }
        )
        HorizontalSpacer(8.dp)
        InputChip(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            label = {
                Text(text = stringResource(Res.string.superfilter_category_fandoms))
            }
        )
        HorizontalSpacer(8.dp)
        InputChip(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            label = {
                Text(text = stringResource(Res.string.superfilter_category_tags))
            }
        )
        HorizontalSpacer(8.dp)
        InputChip(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            label = {
                Text(text = stringResource(Res.string.superfilter_category_directions))
            }
        )
    }
}

@Composable
private fun Page(component: SuperfilterTabComponent) {
    val state by component.state.collectAsState()
    val dialogInstance = component.addValueDialog.subscribeAsState().value.child?.instance

    val hazeState = remember { HazeState() }
    val blurConfig = LocalGlassEffectConfig.current

    dialogInstance?.let {
        AddValueDialog(
            modifier = Modifier.thenIf(blurConfig.blurEnabled) {
                clip(CardDefaults.shape) then hazeChild(
                    state = hazeState,
                    style = blurConfig.style
                )
            },
            component = it,
            containerColor = if(blurConfig.blurEnabled) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    }

    if(state.values.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .thenIf(blurConfig.blurEnabled) {
                    haze(state = hazeState)
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_filter_outlined_crossed),
                    contentDescription = stringResource(Res.string.content_description_icon_filter_crossed),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(160.dp)
                )
                VerticalSpacer(20.dp)
                Text(
                    text = stringResource(Res.string.blacklist_empty),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .thenIf(blurConfig.blurEnabled) {
                    haze(state = hazeState)
                }
        ) {
            items(state.values) { blacklistItem ->
                ValueCardItem(
                    name = blacklistItem.name,
                    value = blacklistItem.value,
                    onRemove = {
                        component.onIntent(
                            SuperfilterTabComponent.Intent.Remove(blacklistItem.value)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ValueCardItem(
    modifier: Modifier = Modifier,
    name: String?,
    value: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = modifier.padding(DefaultPadding.CardDefaultPadding)
    ) {
        Row(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name ?: value,
                modifier = Modifier.weight(1F)
            )
            IconButton(
                onClick = onRemove
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_cancel),
                    contentDescription = stringResource(Res.string.content_description_icon_cancel),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AddValueDialog(
    modifier: Modifier = Modifier,
    component: SuperfilterTabComponent.AddValueDialogComponent,
    containerColor: Color
) {
    val state by component.state.collectAsState()
    DialogPlatform(
        onDismissRequest = component::close,
        modifier = modifier.fillMaxHeight(0.6F)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            )
        ) {
            Column(
                modifier = Modifier.padding(DefaultPadding.CardDefaultPadding)
            ) {
                if(component.searchAvailable) {
                    OutlinedTextField(
                        value = state.searchedName,
                        onValueChange = component::onSearchedNameChange,
                        label = {
                            Text(text = stringResource(Res.string.search_by_name))
                        },
                        singleLine = true,
                        shape = CardDefaults.shape,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                VerticalSpacer(8.dp)
                LazyColumn(
                    modifier = Modifier.weight(1F),
                ) {
                    items(state.values) { value ->
                        VerticalSpacer(8.dp)
                        SearchedValueItem(
                            value = value,
                            onClick = { component.select(value) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = component::close
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchedValueItem(
    modifier: Modifier = Modifier,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalSpacer(DefaultPadding.CardHorizontalPadding)
        Text(
            text = value,
            modifier = Modifier.weight(1F)
        )
        HorizontalSpacer(DefaultPadding.CardHorizontalPadding)
    }
}