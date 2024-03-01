package ru.blays.ficbook.components.userProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.moriatsushi.insetsx.systemBarsPadding
import ficbook_reader.`compose-ui`.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.ui.profileComponents.declaration.UserProfileComponent
import ru.blays.ficbook.utils.surfaceColorAtAlpha
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar
import java.io.File

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
            val widthFill = when(maxWidth) {
                in 1500.dp..Dp.Infinity -> 0.5F
                in 1200.dp..1500.dp -> 0.6F
                in 1000.dp..1200.dp -> 0.7F
                in 800.dp..1000.dp -> 0.8F
                in 500.dp..800.dp -> 0.9F
                else -> 1F
            }

            val avatarPainter = state?.let {
                rememberAsyncImagePainter(model = File(it.avatarPath))
            } ?: painterResource(Res.drawable.ic_incognito)

            Card(
                onClick = {
                    state?.let {
                        component.onOutput(
                            UserProfileComponent.Output.OpenProfile("authors/${it.id}") // TODO - remove href generation from ui
                        )
                    }
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtAlpha(0.3F)
                ),
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPadding)
                    .padding(
                        top = 40.dp,
                        bottom = 100.dp
                    )
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(widthFill),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = avatarPainter,
                            contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
                            contentScale = if(state == null) {
                                ContentScale.Inside
                            } else ContentScale.Crop,
                            colorFilter = if(state == null) {
                                ColorFilter.tint(MaterialTheme.colorScheme.primary)
                            } else null,
                            modifier = Modifier
                                .size(65.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.requiredWidth(12.dp))
                        Text(
                            modifier = Modifier,
                            text = state?.name ?: stringResource(Res.string.anonymous_user),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Start,
                        )
                    }
                    Icon(
                        painter = painterResource(Res.drawable.ic_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.rotate(180F).padding(10.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 40.dp,
                        start = 12.dp,
                        end = 12.dp
                    ),
            ) {
                Button(
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        component.onOutput(
                            UserProfileComponent.Output.ManageAccounts
                        )
                    },
                    modifier = Modifier
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
            }

        }
    }
}