package ru.blays.ficbook.components.users

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ficbook_reader.`compose-ui`.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.data.dto.UserModelStable
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersSearchComponent
import ru.blays.ficbook.values.DefaultPadding

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SearchUsersContent(component: UsersSearchComponent) {
    val state by component.state.subscribeAsState()

    LazyColumn {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.background
                    )
            ) {
                TextField(
                    value = state.searchedName,
                    onValueChange = {
                        component.sendIntent(
                            UsersSearchComponent.Intent.ChangeSearchedName(
                                newName = it
                            )
                        )
                    },
                    label = {
                        Text(text = stringResource(Res.string.search_author))
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                component.sendIntent(
                                    UsersSearchComponent.Intent.Clear
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_cancel),
                                contentDescription = stringResource(Res.string.clear),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = CardDefaults.shape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DefaultPadding.CardDefaultPadding),
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
        items(state.list) { author ->
            AuthorItem(
                author = author,
                onClick = {
                    component.onOutput(
                        UsersRootComponent.Output.OpenAuthorProfile(
                            href = author.href
                        )
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AuthorItem(
    author: UserModelStable,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(author.avatarUrl.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = author.avatarUrl,
                    contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
                    contentScale = ContentScale.Crop,
                    loading = {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(2.dp)
                        )
                    },
                    modifier = Modifier
                        .padding(12.dp)
                        .size(60.dp)
                        .clip(CircleShape)
                )
            }
            Text(
                text = author.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}