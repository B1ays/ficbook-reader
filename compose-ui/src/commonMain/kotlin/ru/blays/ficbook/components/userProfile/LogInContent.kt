package ru.blays.ficbook.components.userProfile

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.moriatsushi.insetsx.systemBarsPadding
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.platformUtils.WindowSize
import ru.blays.ficbook.platformUtils.landscapeModeWidth
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserLogInComponent
import ru.blays.ficbook.ui_components.AutofillTextField.AutofillTextField
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
@Composable
fun LogInContent(component: UserLogInComponent) {
    val state by component.state.subscribeAsState()
    val login = state.login
    val password = state.password
    val loading = state.loading
    val success = state.success
    val logInButtonActive = remember(login, password) {
        login.isNotEmpty() && password.isNotEmpty()
    }

    val logInButtonContainerColor by animateColorAsState(
        targetValue = if(logInButtonActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
                .copy(alpha = 0.12f)
                .compositeOver(MaterialTheme.colorScheme.surface)
        },
        animationSpec = spring()
    )
    val logInButtonContentColor = contentColorFor(logInButtonContainerColor)
    val logInButtonFill by animateFloatAsState(
        targetValue = if(logInButtonActive) 1F else 0.9F,
        animationSpec = spring()
    )

    var showAuthorizationErrorMessage by remember { mutableStateOf(false) }
    var passwordHided by remember { mutableStateOf(true) }

    val windowSize = WindowSize()
    val fillValue by animateFloatAsState(
        targetValue = when {
            windowSize.width > landscapeModeWidth * 2 -> 0.25F
            windowSize.width > landscapeModeWidth * 1.5 -> 0.4F
            windowSize.width > landscapeModeWidth -> 0.6F
            else -> 1F
        },
        animationSpec = spring()
    )

    LaunchedEffect(success) {
        if(!success) showAuthorizationErrorMessage = true
    }

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                UserLogInComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small(stringResource(Res.string.toolbar_title_profile)),
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPaddingLarge)
                    .fillMaxWidth(fillValue)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = CardDefaults.shape
                    )
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(0.1F))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.action_add_account),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.fillMaxHeight(0.04F))
                AnimatedVisibility(
                    visible = showAuthorizationErrorMessage,
                    enter = scaleIn(spring()),
                    exit = scaleOut(spring())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = CardDefaults.shape
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.error_authorization, state.reason ?: ""),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(DefaultPadding.CardDefaultPaddingLarge)
                                .weight(0.8F)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = {
                                showAuthorizationErrorMessage = false
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_cancel),
                                contentDescription = stringResource(Res.string.content_description_icon_cancel),
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.fillMaxHeight(0.04F))
                AutofillTextField(
                    modifier = Modifier.fillMaxWidth(),
                    autofillTypes = listOf(AutofillType.Username),
                    value = login,
                    onValueChange = { login ->
                        component.sendIntent(
                            UserLogInComponent.Intent.LoginChanged(login)
                        )
                    },
                    singleLine = true,
                    label = {
                        Text(text = stringResource(Res.string.login))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = CardDefaults.shape
                )
                Spacer(modifier = Modifier.height(18.dp))
                AutofillTextField(
                    modifier = Modifier.fillMaxWidth(),
                    autofillTypes = listOf(AutofillType.Password),
                    value = password,
                    onValueChange = { password ->
                        component.sendIntent(
                            UserLogInComponent.Intent.PasswordChanged(password)
                        )
                    },
                    singleLine = true,
                    label = {
                        Text(text = stringResource(Res.string.password))
                    },
                    trailingIcon = {
                       IconToggleButton(
                           checked = passwordHided,
                           onCheckedChange = {
                               passwordHided = it
                           }
                       ) {
                           AnimatedContent(
                               targetState = if(passwordHided) Res.drawable.ic_eye_crossed else Res.drawable.ic_eye_filled
                           ) {
                               Icon(
                                   painter = painterResource(it),
                                   contentDescription = stringResource(Res.string.content_description_icon_eye_crossed),
                                   modifier = Modifier.size(24.dp),
                                   tint = MaterialTheme.colorScheme.onSurface
                               )
                           }
                       }
                    },
                    visualTransformation = if (passwordHided) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = CardDefaults.shape
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(logInButtonFill)
                        .height(56.dp),
                    enabled = !loading && logInButtonActive,
                    onClick = {
                        component.sendIntent(UserLogInComponent.Intent.LogIn)
                    },
                    shape = CardDefaults.shape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = logInButtonContainerColor,
                        contentColor = logInButtonContentColor,
                        disabledContainerColor = logInButtonContainerColor,
                        disabledContentColor = logInButtonContentColor
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.action_log_in),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}