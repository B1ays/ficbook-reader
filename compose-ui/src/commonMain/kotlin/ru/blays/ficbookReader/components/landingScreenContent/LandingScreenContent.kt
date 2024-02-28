@file:OptIn(ExperimentalResourceApi::class)

package ru.blays.ficbookReader.components.landingScreenContent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import ficbook_reader.`compose-ui`.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ru.blays.ficbookReader.shared.ui.landingScreenComponent.ConfirmDialogComponent
import ru.blays.ficbookReader.shared.ui.landingScreenComponent.ConfirmDialogConfig
import ru.blays.ficbookReader.shared.ui.landingScreenComponent.LandingScreenComponent

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
                text = "Добро пожаловать в Ficbook Reader",
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
                        text = "Добавить аккаунт"
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
                        text = "Зарегистрироваться"
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
                        text = "Анонимный режим"
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
                    text = "Отмена"
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

@Composable
private fun getTitleForConfig(config: ConfirmDialogConfig): String {
    return remember(config) {
        when(config) {
            is ConfirmDialogConfig.ConfirmAnonymousMode -> "Пропустить авторизацию?"
            is ConfirmDialogConfig.ConfirmDBMigration -> "Добавить сохранённый аккаунт?"
            is ConfirmDialogConfig.ConfirmRegisterRedirect -> "Перейти на сайт?"
            is ConfirmDialogConfig.DBMigrationFailed -> "Не удалось конвертировать."
        }
    }
}

@Composable
private fun getDescriptionForConfig(config: ConfirmDialogConfig): String {
    return remember(config) {
        when(config) {
            is ConfirmDialogConfig.ConfirmAnonymousMode -> "Ficbook Reader можно использоваться и без аккаунта ficbook, но в этом случае многие разделы сайта окажутся не доступны.\n" +
                "Чтобы войти в аккаунт позже, необходимо нажать на кнопку профиля в правом верхнем углу экрана."
            is ConfirmDialogConfig.ConfirmDBMigration -> "Найден сохранённый аккаунт старого формата.\n" +
                "Конвертировать его в новый формат?"
            is ConfirmDialogConfig.ConfirmRegisterRedirect -> "Регистрация выполняется на сайте ficbook.net."
            is ConfirmDialogConfig.DBMigrationFailed -> "Автоматическая конвертация не удалась.\n" +
                "Попробуйте ещё раз позже. Или заново войдите в аккаунт."
        }
    }
}

@Composable
private fun getActionNameForConfig(config: ConfirmDialogConfig): String {
    return remember(config) {
        when(config) {
            is ConfirmDialogConfig.ConfirmAnonymousMode -> "Пропустить"
            is ConfirmDialogConfig.ConfirmDBMigration -> "Подтвердить"
            is ConfirmDialogConfig.ConfirmRegisterRedirect -> "Перейти"
            is ConfirmDialogConfig.DBMigrationFailed -> "Перейти"
        }
    }
}