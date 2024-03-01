@file:OptIn(ExperimentalResourceApi::class)

package ru.blays.ficbook.components.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ficbook_reader.`compose-ui`.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.platformUtils.onPointerEventPlatform
import ru.blays.ficbook.reader.shared.data.dto.FanficDirection
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.PopularSectionsComponent
import ru.blays.ficbook.theme.getColorForDirection
import ru.blays.ficbook.values.DefaultPadding

@Composable
fun PopularCategoriesContent(
    component: PopularSectionsComponent,
    contentPadding: PaddingValues?
) {
    val sections = component.sections

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        if(maxWidth > 520.dp) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                verticalArrangement = Arrangement.spacedBy(space = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 2.dp),
                contentPadding = contentPadding ?: PaddingValues(0.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize()
            ) {
                items(sections) { (section, direction) ->
                    CategoryGridItem(section, direction) {
                        component.onOutput(
                            PopularSectionsComponent.Output.NavigateToSection(section)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = contentPadding ?: PaddingValues(0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sections) { (section, direction) ->
                    CategoryListItem(section, direction) {
                        component.onOutput(
                            PopularSectionsComponent.Output.NavigateToSection(section)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CategoryListItem(
    section: SectionWithQuery,
    direction: FanficDirection,
    onClick: () -> Unit
) {
    val primaryColor = remember(direction) { getColorForDirection(direction) }
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if(isFocused) 1.03F else 1F
    )
    val iconSize by animateDpAsState(
        targetValue = if(isFocused) 40.dp else 30.dp
    )
    val iconColor by animateColorAsState(
        targetValue = if(isFocused) primaryColor else onSurfaceColor
    )

    val icon = iconForDirection(direction)

    Box(
        modifier = Modifier
            .padding(
                DefaultPadding.CardDefaultPadding
            )
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                shape = CardDefaults.shape,
                clip = true
            )
            .clickable(onClick = onClick)
            .onPointerEventPlatform(PointerEventType.Enter) { isFocused = true }
            .onPointerEventPlatform(PointerEventType.Exit) { isFocused = false },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = stringResource(Res.string.content_description_icon_direction),
                modifier = Modifier.size(iconSize),
                tint = iconColor
            )
            Text(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPadding)
                    .fillMaxWidth(),
                text = section.name,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CategoryGridItem(
    section: SectionWithQuery,
    direction: FanficDirection,
    onClick: () -> Unit
) {
    val primaryColor = remember(direction) { getColorForDirection(direction) }
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if(isFocused) 1.03F else 1F
    )
    val iconSize by animateDpAsState(
        targetValue = if(isFocused) 50.dp else 34.dp
    )
    val iconColor by animateColorAsState(
        targetValue = if(isFocused) primaryColor else onSurfaceColor
    )

    val icon = iconForDirection(direction)

    Box(
        modifier = Modifier
            .height(60.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                shape = CardDefaults.shape,
                clip = true
            )
            .clickable(onClick = onClick)
            .onPointerEventPlatform(PointerEventType.Enter) { isFocused = true }
            .onPointerEventPlatform(PointerEventType.Exit) { isFocused = false },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = stringResource(Res.string.content_description_icon_direction),
                modifier = Modifier.size(iconSize),
                tint = iconColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPadding)
                    .fillMaxWidth(),
                text = section.name,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

private val iconForDirection: @Composable (direction: FanficDirection) -> Painter
    @Composable get() = { direction ->
        when (direction) {
            FanficDirection.GEN -> painterResource(Res.drawable.ic_direction_gen)
            FanficDirection.HET -> painterResource(Res.drawable.ic_direction_het)
            FanficDirection.SLASH -> painterResource(Res.drawable.ic_direction_slash)
            FanficDirection.FEMSLASH -> painterResource(Res.drawable.ic_direction_femslash)
            FanficDirection.ARTICLE -> painterResource(Res.drawable.ic_direction_article)
            FanficDirection.MIXED -> painterResource(Res.drawable.ic_direction_mixed)
            FanficDirection.OTHER -> painterResource(Res.drawable.ic_direction_other)
            FanficDirection.UNKNOWN -> painterResource(Res.drawable.ic_direction_other)
        }
    }