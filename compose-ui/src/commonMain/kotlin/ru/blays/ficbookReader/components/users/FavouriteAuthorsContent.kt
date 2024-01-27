package ru.blays.ficbookReader.components.users

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ru.blays.ficbookReader.shared.data.dto.UserModelStable
import ru.blays.ficbookReader.shared.ui.usersComponent.declaration.UsersFavouriteComponent
import ru.blays.ficbookReader.shared.ui.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbookReader.values.DefaultPadding

@Composable
fun FavouriteAuthorsContent(component: UsersFavouriteComponent) {
    val state by component.state.subscribeAsState()

    val lazyListState = rememberLazyListState()

    val canScrollForward = lazyListState.canScrollForward
    val canScrollBackward = lazyListState.canScrollBackward

    LaunchedEffect(canScrollForward) {
        if(!canScrollForward && canScrollBackward) {
            component.sendIntent(
                UsersFavouriteComponent.Intent.LoadNextPage
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = lazyListState
    ) {
        items(state.list) { author ->
            AuthorItem(author) {
                component.onOutput(
                    UsersRootComponent.Output.OpenAuthorProfile(
                        href = author.href
                    )
                )
            }
        }
    }
}

@Composable
private fun AuthorItem(
    author: UserModelStable,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = author.name,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(12.dp),
        )
    }
}