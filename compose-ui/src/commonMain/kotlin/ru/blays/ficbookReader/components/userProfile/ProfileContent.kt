package ru.blays.ficbookReader.components.userProfile

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.systemBarsPadding
import io.github.skeptick.libres.compose.painterResource
import ru.blays.ficbookReader.shared.ui.profileComponents.UserProfileComponent
import ru.blays.ficbookReader.utils.surfaceColorAtAlpha
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar

@Composable
fun UserProfileContent(component: UserProfileComponent) {
    val state by component.state.collectAsState()

    Scaffold(
        topBar = {
            CollapsingsToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                UserProfileComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_arrow_back),
                            contentDescription = "Стрелка назад"
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large("Профиль")
            )
        },
        modifier = Modifier.systemBarsPadding(),
    ) { padding ->
        if(state != null) {
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

                Card(
                    onClick = {
                        component.onOutput(
                            UserProfileComponent.Output.OpenProfile(state?.href!!)
                        )
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
                            SubcomposeAsyncImage(
                                model = state?.avatarUrl!!,
                                contentDescription = "Аватар пользователя",
                                contentScale = ContentScale.Crop,
                                loading = {
                                    CircularProgressIndicator()
                                },
                                modifier = Modifier
                                    .size(65.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.requiredWidth(12.dp),)
                            Text(
                                modifier = Modifier,
                                text = state?.name!!,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Start,
                            )
                        }
                        Icon(
                            painter = painterResource(Res.image.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier.rotate(180F).padding(10.dp)
                        )
                    }
                }
                Button(
                    onClick = {
                        component.sendIntent(
                            UserProfileComponent.Intent.LogOut
                        )
                    },
                    shape = CardDefaults.shape,
                    modifier = Modifier
                        .padding(DefaultPadding.CardDefaultPadding)
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(widthFill)
                        .requiredHeight(50.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Icon(
                        painter = painterResource(Res.image.ic_exit),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.requiredWidth(10.dp))
                    Text(
                        text = "Выйти из аккаунта",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}