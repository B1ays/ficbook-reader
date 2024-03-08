package ru.blays.ficbook.components.landingScreenContent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ficbook_reader.`compose-ui`.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.components.landingScreenComponent.ConfirmDialogComponent
import ru.blays.ficbook.reader.shared.components.landingScreenComponent.ConfirmDialogConfig
import ru.blays.ficbook.reader.shared.components.landingScreenComponent.LandingScreenComponent

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LandingScreenContent(component: LandingScreenComponent) {
    val dialogState by component.confirmDialog.subscribeAsState()
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
        sheetContent = {
            dialogState.child?.let {
                val instance = it.instance
                val config = it.configuration

                ConfirmDialogContent(instance, config)
            }
        },
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = false,
        scaffoldState = bottomSheetScaffoldState
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
        ) {
            Text(
                text = stringResource(Res.string.landing_title),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.8F)
                    .align(Alignment.Center)
                    .offset(y = (-200).dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.8F),
            ) {
                Button(
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        component.sendIntent(
                            LandingScreenComponent.Intent.AddNewAccount
                        )
                    },
                    modifier = Modifier
                        .height(44.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_user_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(Res.string.action_add_account)
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))
                OutlinedButton(
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        component.sendIntent(
                            LandingScreenComponent.Intent.Register
                        )
                    },
                    modifier = Modifier
                        .height(44.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_user_edit),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(Res.string.action_register)
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))
                OutlinedButton(
                    shape = MaterialTheme.shapes.medium,
                    onClick = {
                        component.sendIntent(
                            LandingScreenComponent.Intent.EnableAnonymousMode
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
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(dialogState.child) {
        scope.launch {
            if(dialogState.child != null) {
                bottomSheetScaffoldState.bottomSheetState.expand()
            } else {
                bottomSheetScaffoldState.bottomSheetState.partialExpand()
            }
        }

    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ConfirmDialogContent(
    instance: ConfirmDialogComponent,
    config: ConfirmDialogConfig
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxWidth(),
    ) {
        Text(
            text = getTitleForConfig(config),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = getDescriptionForConfig(config),
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                shape = MaterialTheme.shapes.small,
                onClick = {
                    instance.sendIntent(
                        ConfirmDialogComponent.Intent.Cancel
                    )
                },
                modifier = Modifier
                    .height(40.dp)
                    .weight(0.4F, true)
            ) {
                Text(
                    text = stringResource(Res.string.cancel)
                )
            }
            Button(
                shape = MaterialTheme.shapes.small,
                onClick = {
                    instance.sendIntent(
                        ConfirmDialogComponent.Intent.Confirm
                    )
                },
                modifier = Modifier
                    .height(40.dp)
                    .weight(0.4F, true)
            ) {
                Text(
                    text = getActionNameForConfig(config)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun getTitleForConfig(config: ConfirmDialogConfig): String {
    return when(config) {
        is ConfirmDialogConfig.ConfirmAnonymousMode -> stringResource(Res.string.landing_dialog_title_confirm_am)
        is ConfirmDialogConfig.ConfirmDBMigration -> stringResource(Res.string.landing_dialog_title_confirm_migration)
        is ConfirmDialogConfig.ConfirmRegisterRedirect -> stringResource(Res.string.landing_dialog_title_confirm_redirect)
        is ConfirmDialogConfig.DBMigrationFailed -> stringResource(Res.string.landing_dialog_title_migration_failed)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun getDescriptionForConfig(config: ConfirmDialogConfig): String {
    return when(config) {
        is ConfirmDialogConfig.ConfirmAnonymousMode -> stringResource(Res.string.landing_dialog_description_confirm_am)
        is ConfirmDialogConfig.ConfirmDBMigration -> stringResource(Res.string.landing_dialog_description_confirm_migration)
        is ConfirmDialogConfig.ConfirmRegisterRedirect -> stringResource(Res.string.landing_dialog_description_confirm_redirect)
        is ConfirmDialogConfig.DBMigrationFailed -> stringResource(Res.string.landing_dialog_description_migration_failed)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun getActionNameForConfig(config: ConfirmDialogConfig): String {
    return when(config) {
        is ConfirmDialogConfig.ConfirmAnonymousMode -> stringResource(Res.string.action_skip)
        is ConfirmDialogConfig.ConfirmDBMigration -> stringResource(Res.string.action_confirm)
        is ConfirmDialogConfig.ConfirmRegisterRedirect -> stringResource(Res.string.action_go_to)
        is ConfirmDialogConfig.DBMigrationFailed -> stringResource(Res.string.action_go_to)
    }
}