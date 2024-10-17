package ru.blays.ficbook.components.aboutContent

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.SharedBuildKonfig
import ru.blays.ficbook.reader.shared.platformUtils.openInBrowser
import ru.blays.ficbook.ui_components.LazyItems.itemWithHeader
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.LocalBlurState
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun AboutContent(onBack: () -> Unit) {
    val hazeState = remember { HazeState() }
    val blurEnabled = LocalBlurState.current

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.toolbar_title_about)),
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
        }
    ) { padding ->
        val appGroupTitle = stringResource(Res.string.about_group_app)
        val developerGroupTitle = stringResource(Res.string.about_group_developer)

        LazyColumn(
            contentPadding = padding,
            modifier = Modifier,
        ) {
            item { Header() }
            itemWithHeader(appGroupTitle) {
                AppGroup()
            }
            itemWithHeader(developerGroupTitle) {
                DeveloperGroup()
            }
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_app),
            contentDescription = null,
            modifier = Modifier.size(86.dp),
        )
        VerticalSpacer(10.dp)
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        VerticalSpacer(2.dp)
        Text(
            text = "${SharedBuildKonfig.versionNameFull} (${SharedBuildKonfig.versionCode})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.W300
        )
    }
}

@Composable
fun AppGroup() {
    val scope = rememberCoroutineScope()
    ItemCard(
        title = stringResource(Res.string.about_title_tg_group),
        iconPainter = painterResource(Res.drawable.ic_telegram),
        shape = CardShape.CardStart,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        scope.launch {
            openInBrowser(getString(Res.string.link_tg_group))
        }
    }
    ItemCard(
        title = stringResource(Res.string.about_title_source_code),
        iconPainter = painterResource(Res.drawable.ic_github),
        shape = CardShape.CardEnd,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        scope.launch {
            openInBrowser(getString(Res.string.link_github_repo))
        }
    }
}

@Composable
fun DeveloperGroup() {
    val scope = rememberCoroutineScope()
    InfoCard(
        text = stringResource(Res.string.about_info_developer),
        iconPainter = painterResource(Res.drawable.ic_user),
        shape = CardShape.CardStart,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    )
    ItemCard(
        title = stringResource(Res.string.about_title_profile_tg),
        iconPainter = painterResource(Res.drawable.ic_telegram),
        shape = CardShape.CardMid,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        scope.launch {
            openInBrowser(getString(Res.string.link_developer_tg))
        }
    }
    ItemCard(
        title = stringResource(Res.string.about_title_profile_4pda),
        iconPainter = painterResource(Res.drawable.ic_4pda),
        shape = CardShape.CardMid,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        scope.launch {
            openInBrowser(getString(Res.string.link_developer_4pda))
        }
    }
    ItemCard(
        title = stringResource(Res.string.about_title_profile_github),
        iconPainter = painterResource(Res.drawable.ic_github),
        shape = CardShape.CardMid,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        scope.launch {
            openInBrowser(getString(Res.string.link_developer_github))
        }
    }
    ItemCard(
        title = stringResource(Res.string.about_title_support),
        iconPainter = painterResource(Res.drawable.ic_usd_circle),
        shape = CardShape.CardEnd,
        modifier = Modifier.padding(DefaultPadding.CardPaddingSmallVertical),
    ) {
        scope.launch {
            openInBrowser(getString(Res.string.link_developer_support))
        }
    }
}

@Composable
fun ItemCard(
    title: String,
    subtitle: String? = null,
    iconPainter: Painter,
    iconSize: Dp = 36.dp,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.elevatedCardColors(
        contentColor = MaterialTheme.colorScheme.primary.copy(0.8F)
    ),
    elevation: Dp = 10.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.clip(shape),
        onClick = onClick,
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
            HorizontalSpacer(12.dp)
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if(subtitle != null) {
                    VerticalSpacer(4.dp)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    text: String,
    iconPainter: Painter,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.elevatedCardColors(
        contentColor = MaterialTheme.colorScheme.primary.copy(0.8F)
    ),
    elevation: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.clip(shape),
        shape = shape,
        colors = colors,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            HorizontalSpacer(10.dp)
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun CreditCard(
    credit: String,
    creditFor: String,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.elevatedCardColors(
        contentColor = MaterialTheme.colorScheme.primary.copy(0.8F)
    ),
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.clip(shape),
        onClick = onClick,
        shape = shape,
        colors = colors
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = credit,
                style = MaterialTheme.typography.titleMedium
            )
            VerticalSpacer(4.dp)
            Text(
                text = creditFor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}