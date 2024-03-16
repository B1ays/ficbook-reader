package ru.blays.ficbook.components.userProfile

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.moriatsushi.insetsx.systemBarsPadding
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileComponent
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.CornerSmoothing
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.SquircleShape
import ru.blays.ficbook.ui_components.spacers.HorizontalSpacer
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@OptIn(ExperimentalResourceApi::class)
@Composable
fun UserProfileContent(component: UserProfileComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                UserProfileComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.toolbar_title_profile))
            )
        },
        modifier = Modifier.systemBarsPadding(),
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
        ) {
            val widthFill = when (maxWidth) {
                in 1500.dp..Dp.Infinity -> 0.5F
                in 1200.dp..1500.dp -> 0.6F
                in 1000.dp..1200.dp -> 0.7F
                in 800.dp..1000.dp -> 0.8F
                in 500.dp..800.dp -> 0.9F
                else -> 1F
            }

            val avatarShape = SquircleShape(
                cornerSmoothing = CornerSmoothing.High
            )

            val textBlock: @Composable ColumnScope.() -> Unit = {
                Text(
                    modifier = Modifier,
                    text = state?.name ?: stringResource(Res.string.anonymous_user),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1
                )
                state?.id?.let { id ->
                    VerticalSpacer(4.dp)
                    Text(
                        modifier = Modifier,
                        text = "ID: $id",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            val avatarImage: @Composable (modifier: Modifier) -> Unit = { modifier ->
                SubcomposeAsyncImage(
                    model = state?.avatarPath,
                    contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
                    contentScale = ContentScale.Crop,
                    success = { state ->
                        Image(
                            painter = state.painter,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)
                                )
                        )
                    },
                    error = {
                        Image(
                            painter = painterResource(Res.drawable.ic_incognito),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    },
                    modifier = modifier
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(widthFill),
            ) {
                AnimatedContent(
                    targetState = widthFill > 0.9F,
                    modifier = Modifier.weight(1F)
                ) { inColumn ->
                    if (inColumn) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.fillMaxHeight(0.1F))
                            avatarImage(
                                Modifier
                                    .size(140.dp)
                                    .clip(avatarShape)
                                    .clickable {
                                        component.onOutput(
                                            UserProfileComponent.Output.OpenProfile()
                                        )
                                    }
                            )
                            VerticalSpacer(16.dp)
                            textBlock()
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier,
                        ) {
                            avatarImage(
                                Modifier
                                    .size(140.dp * widthFill)
                                    .clip(avatarShape)
                                    .clickable {
                                        component.onOutput(
                                            UserProfileComponent.Output.OpenProfile()
                                        )
                                    }
                            )
                            HorizontalSpacer(20.dp)
                            Column(content = textBlock)
                        }
                    }
                }

                Button(
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        component.onOutput(
                            UserProfileComponent.Output.ManageAccounts
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = DefaultPadding.CardHorizontalPadding)
                        .height(44.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_replace),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(Res.string.action_change_account)
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))
                OutlinedButton(
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        component.sendIntent(
                            UserProfileComponent.Intent.EnableIncognito
                        )
                    },
                    modifier = Modifier
                        .padding(horizontal = DefaultPadding.CardHorizontalPadding)
                        .height(44.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_incognito),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(Res.string.anonymous_mode)
                    )
                }
                VerticalSpacer(40.dp)
            }
        }
    }
}