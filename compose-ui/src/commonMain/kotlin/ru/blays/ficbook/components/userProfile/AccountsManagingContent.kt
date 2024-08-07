package ru.blays.ficbook.components.userProfile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileManagingComponent
import ru.blays.ficbook.reader.shared.data.dto.SavedUserModel
import ru.blays.ficbook.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.CornerSmoothing
import ru.blays.ficbook.ui_components.CustomShape.SquircleShape.SquircleShape
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar
import java.io.File

@Composable
fun AccountsManagingContent(component: UserProfileManagingComponent) {
    val state by component.state.subscribeAsState()

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                UserProfileManagingComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.toolbar_title_account_managing)),
            )
        },
        modifier = Modifier.systemBarsPadding(),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            LazyColumn(
                modifier = Modifier.weight(1F),
            ) {
                items(state.savedUsers) { user ->
                    SavedUserItem(
                        user = user,
                        selected = user.id == state.selectedUserID,
                        onSelect = {
                            component.sendIntent(
                                UserProfileManagingComponent.Intent.ChangeUser(user.id)
                            )
                        },
                        onDelete = {
                            component.sendIntent(
                                UserProfileManagingComponent.Intent.DeleteUser(user.id)
                            )
                        }
                    )
                }
            }
            Button(
                shape = MaterialTheme.shapes.medium,
                onClick = {
                    component.sendIntent(
                        UserProfileManagingComponent.Intent.AddNewAccount
                    )
                },
                modifier = Modifier
                    .height(44.dp)
                    .fillMaxWidth(0.8F)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_plus),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(Res.string.action_add_account)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SavedUserItem(
    user: SavedUserModel,
    selected: Boolean,
    onDelete: () -> Unit,
    onSelect: () -> Unit
) {
    AnimatedContent(
        targetState = selected,
        transitionSpec = {
            fadeIn(spring()) togetherWith fadeOut(spring())
        }
    ) {
        if(it) {
            Card(
                onClick = onSelect,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(0.3F)
                ),
                modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingLarge)
            ) {
                UserCardContent(
                    user = user,
                    onDelete = onDelete
                )
            }
        } else {
            OutlinedCard(
                onClick = onSelect,
                modifier = Modifier.padding(DefaultPadding.CardDefaultPaddingLarge)
            ) {
                UserCardContent(
                    user = user,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
fun UserCardContent(
    user: SavedUserModel,
    onDelete: () -> Unit
) {
    val avatarShape = SquircleShape(
        cornerSmoothing = CornerSmoothing.High
    )

    val imageContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp)

    Row(
        modifier = Modifier
            .padding(14.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubcomposeAsyncImage(
            model = File(user.avatarPath),
            contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
            contentScale = ContentScale.Crop,
            success = { state ->
                Image(
                    painter = state.painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = imageContainerColor
                        )
                )
            },
            modifier = Modifier
                .size(65.dp)
                .clip(avatarShape)
        )
        Spacer(modifier = Modifier.requiredWidth(12.dp))
        Column(
            modifier = Modifier.weight(1F),
        ) {
            Text(
                modifier = Modifier,
                text = user.name,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier,
                text = "ID: ${user.id}",
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Start,
            )
        }
        Spacer(modifier = Modifier.requiredWidth(10.dp))
        CustomIconButton(
            onClick = onDelete,
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = imageContainerColor,
            minSize = 42.dp
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = stringResource(Res.string.content_description_icon_delete),
                modifier = Modifier.size(30.dp),
            )
        }
    }
}