package ru.blays.ficbookReader.components.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ficbook_reader.`compose-ui`.generated.resources.Res
import ficbook_reader.`compose-ui`.generated.resources.content_description_icon_author_avatar
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbookReader.shared.data.dto.PopularAuthorModelStable
import ru.blays.ficbookReader.shared.ui.usersComponent.declaration.UsersPopularComponent
import ru.blays.ficbookReader.shared.ui.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbookReader.theme.likeColor
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
fun PopularAuthorsContent(
    component: UsersPopularComponent
) {
    val state by component.state.subscribeAsState()

    if(!state.loading) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.list) { author ->
                AuthorItem(author) {
                    component.onOutput(
                        UsersRootComponent.Output.OpenAuthorProfile(
                            href = author.user.href
                        )
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxWidth(0.5F),
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AuthorItem(
    author: PopularAuthorModelStable,
    onAuthorClick: () -> Unit
) {
    Card(
        onClick = onAuthorClick,
        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = author.user.avatarUrl,
                contentDescription = stringResource(Res.string.content_description_icon_author_avatar),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(0.2F)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = 8.dp,
                        vertical = 3.dp
                    )
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = author.user.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(0.7F),
                    )
                    Text(
                        text = "№${author.position}",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        color = likeColor,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.3F),
                    )
                }
                Spacer(modifier = Modifier.requiredHeight(14.dp))
                Text(
                    text = author.subscribersInfo,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}