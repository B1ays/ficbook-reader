package ru.blays.ficbook.components.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.LocalHazeStyle
import ficbook_reader.compose_ui.generated.resources.Res
import ficbook_reader.compose_ui.generated.resources.ok
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.components.aboutContent.AboutContent
import ru.blays.ficbook.components.authorProfile.AuthorProfileContent
import ru.blays.ficbook.components.collectionContent.CollectionPageContent
import ru.blays.ficbook.components.fanficPage.FanficPageContent
import ru.blays.ficbook.components.fanficsList.FanficsListScreenContent
import ru.blays.ficbook.components.landingScreenContent.LandingScreenContent
import ru.blays.ficbook.components.main.MainContent
import ru.blays.ficbook.components.notifications.NotificationsContent
import ru.blays.ficbook.components.searchContent.SearchContent
import ru.blays.ficbook.components.settingsContent.SettingsRootContent
import ru.blays.ficbook.components.userProfile.UserProfileRootContent
import ru.blays.ficbook.components.users.UsersRootContent
import ru.blays.ficbook.platformUtils.landscapeInsetsPadding
import ru.blays.ficbook.reader.shared.components.RootComponent.RootComponent
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.DefaultSnackbarVisuals
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.SnackbarHost.snackbarHostState
import ru.blays.ficbook.reader.shared.platformUtils.blurSupported
import ru.blays.ficbook.utils.LocalBlurState
import ru.blays.ficbook.utils.defaultPredictiveAnimation

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootContent(component: RootComponent) {
    val glassEffectConfig by component.glassEffectConfig.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val hazeStyle = remember(glassEffectConfig, backgroundColor) {
        HazeDefaults.style(
            blurRadius = glassEffectConfig.blurRadius.dp,
            noiseFactor = glassEffectConfig.noiseFactor,
            backgroundColor = backgroundColor
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                when(
                    val visuals = data.visuals
                ) {
                    is DefaultSnackbarVisuals.SnackbarVisualsWithError -> {
                        SnackbarVisualsWithError(
                            message = visuals.message,
                            onDismiss = data::dismiss
                        )
                    }
                    is DefaultSnackbarVisuals.SnackbarVisualsWithInfo -> {
                        SnackbarVisualsWithInfo(
                            message = visuals.message
                        )
                    }
                    else -> { Snackbar(data) }
                }
            }
        }
    ) {
        CompositionLocalProvider(
            LocalHazeStyle provides hazeStyle,
            LocalBlurState provides (glassEffectConfig.enabled && blurSupported)
        ) {
            Children(
                stack = component.childStack,
                animation = defaultPredictiveAnimation(
                    backHandler = component.backHandler,
                    onBack = component::navigateBack
                ),
                modifier = Modifier.landscapeInsetsPadding()
            ) {
                when(
                    val child = it.instance
                ) {
                    is RootComponent.Child.FanficPage -> FanficPageContent(child.component)
                    is RootComponent.Child.UserProfile -> UserProfileRootContent(child.component)
                    is RootComponent.Child.Main -> MainContent(child.component)
                    is RootComponent.Child.Settings -> SettingsRootContent(child.component)
                    is RootComponent.Child.FanficsList -> FanficsListScreenContent(child.component)
                    is RootComponent.Child.AuthorProfile -> AuthorProfileContent(child.component)
                    is RootComponent.Child.Collection -> CollectionPageContent(child.component)
                    is RootComponent.Child.Users -> UsersRootContent(child.component)
                    is RootComponent.Child.Notifications -> NotificationsContent(child.component)
                    is RootComponent.Child.Search -> SearchContent(child.component)
                    is RootComponent.Child.Landing -> LandingScreenContent(child.component)
                    is RootComponent.Child.About -> AboutContent(child.onBack)
                }
            }
        }
    }
}


@Composable
fun SnackbarVisualsWithError(
    message: String,
    onDismiss: () -> Unit
) = Snackbar(
    modifier = Modifier.padding(12.dp),
    containerColor = MaterialTheme.colorScheme.errorContainer,
    contentColor = MaterialTheme.colorScheme.onErrorContainer,
    action = {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(Res.string.ok))
        }
    }
) {
    Text(
        text = message,
        maxLines = 2
    )
}


@Composable
fun SnackbarVisualsWithInfo(
    message: String
) = Snackbar(
    modifier = Modifier.padding(12.dp),
) {
    Text(
        text = message,
        maxLines = 2
    )
}