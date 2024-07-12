package ru.blays.ficbook.components.userProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
import coil3.compose.SubcomposeAsyncImage
import ficbook_reader.compose_ui.generated.resources.*
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
        BoxWithConstraints {
            val useLandscapeConstraints = maxWidth > maxHeight
            val constraintsSet = remember(useLandscapeConstraints) {
                ConstraintSet {
                    val avatar = createRefFor(LayoutIds.Avatar)
                    val name = createRefFor(LayoutIds.Name)
                    val id = createRefFor(LayoutIds.Id)
                    val changeButton = createRefFor(LayoutIds.ChangeButton)
                    val anonymousButton = createRefFor(LayoutIds.AnonymousButton)
                    val horizontalSpacer = createRefFor(LayoutIds.HorizontalSpacer)
                    val verticalSpacer = createRefFor(LayoutIds.VerticalSpacer)

                    val bottomGuideline = if(useLandscapeConstraints) {
                        createGuidelineFromBottom(10.dp)
                    } else {
                        createGuidelineFromBottom(40.dp)
                    }

                    if(useLandscapeConstraints) {
                        val horizontalChain = createHorizontalChain(
                            avatar,
                            horizontalSpacer,
                            name,
                            chainStyle = ChainStyle.Packed
                        )
                        val verticalChain = createVerticalChain(
                            name,
                            verticalSpacer,
                            id,
                            chainStyle = ChainStyle.Packed
                        )
                        constrain(horizontalSpacer) {
                            width = Dimension.value(14.dp)
                        }
                        constrain(verticalSpacer) {
                            height = Dimension.value(6.dp)
                        }
                        constrain(horizontalChain) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        constrain(verticalChain) {
                            top.linkTo(avatar.top)
                            bottom.linkTo(avatar.bottom)
                        }
                    }
                    constrain(avatar) {
                        if(useLandscapeConstraints) {
                            top.linkTo(parent.top)
                            bottom.linkTo(changeButton.top, 8.dp)
                            height = Dimension.fillToConstraints.atMost(260.dp)
                            width = Dimension.ratio("1:1")
                        } else {
                            centerHorizontallyTo(parent)
                            centerVerticallyTo(parent, bias = 0.2F)
                            width = Dimension.percent(0.4F)
                            height = Dimension.ratio("1:1")
                        }
                    }
                    constrain(name) {
                        if(!useLandscapeConstraints) {
                            top.linkTo(avatar.bottom, margin = 16.dp)
                            centerHorizontallyTo(avatar)
                        }
                    }
                    constrain(id) {
                        if(useLandscapeConstraints) {
                            start.linkTo(name.start)
                        } else {
                            top.linkTo(name.bottom, 4.dp)
                            bottom.linkTo(changeButton.top, 4.dp)
                            height = Dimension.fillToConstraints
                            centerHorizontallyTo(name)
                        }
                    }
                    constrain(anonymousButton) {
                        bottom.linkTo(bottomGuideline)
                    }
                    constrain(changeButton) {
                        bottom.linkTo(anonymousButton.top, margin = 10.dp)
                    }
                }
            }
            ConstraintLayout(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .fillMaxSize(),
                constraintSet = constraintsSet,
                animateChanges = true,
            ) {
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
                                .clickable {
                                    component.onOutput(
                                        UserProfileComponent.Output.OpenProfile()
                                    )
                                }
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
                    modifier = Modifier
                        .clip(SquircleShape(cornerSmoothing = CornerSmoothing.High))
                        .layoutId(LayoutIds.Avatar)
                )
                Text(
                    modifier = Modifier.layoutId(LayoutIds.Name),
                    text = state?.name ?: stringResource(Res.string.anonymous_user),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1
                )
                state?.id?.let { id ->
                    VerticalSpacer(4.dp)
                    Text(
                        modifier = Modifier.layoutId(LayoutIds.Id),
                        text = "ID: $id",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
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
                        .layoutId(LayoutIds.ChangeButton)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_replace),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    HorizontalSpacer(5.dp)
                    Text(
                        text = stringResource(Res.string.action_change_account)
                    )
                }
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
                        .layoutId(LayoutIds.AnonymousButton)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_incognito),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    HorizontalSpacer(5.dp)
                    Text(
                        text = stringResource(Res.string.anonymous_mode)
                    )
                }
            }
        }
    }
}

private enum class LayoutIds {
    Avatar,
    Name,
    Id,
    ChangeButton,
    AnonymousButton,
    HorizontalSpacer,
    VerticalSpacer
}