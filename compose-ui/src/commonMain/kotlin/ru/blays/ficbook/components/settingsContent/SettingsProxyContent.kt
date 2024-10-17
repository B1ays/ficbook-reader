package ru.blays.ficbook.components.settingsContent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.platformUtils.WindowSize
import ru.blays.ficbook.platformUtils.scaleContent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsProxyComponent
import ru.blays.ficbook.reader.shared.preferences.json.ProxyConfig
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.LocalBlurState
import ru.blays.ficbook.utils.isNotBlankOrEmpty
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar
import java.net.Proxy

@Composable
fun SettingsProxyContent(component: SettingsProxyComponent) {
    val state by component.state.collectAsState()

    val windowSize = WindowSize()
    val widthFill = if(scaleContent) {
        when (windowSize.width) {
            in 1300..Int.MAX_VALUE -> 0.6F
            in 1000..1300 -> 0.7F
            in 800..1000 -> 0.8F
            in 600..800 -> 0.9F
            else -> 1F
        }
    } else 1F

    val blurEnabled = LocalBlurState.current
    val hazeState = remember { HazeState() }

    var useCustomProxy by remember { mutableStateOf(state.usedCustom) }

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                SettingsProxyComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small(stringResource(Res.string.proxy_title)),
                containerColor = if(blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(blurEnabled) {
                    hazeChild(state = hazeState)
                },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(DefaultPadding.CardDefaultPadding)
                    .fillMaxWidth(widthFill)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    onClick = {
                        component.sendIntent(
                            SettingsProxyComponent.Intent.ChangeProxyEnabled(!state.enabled)
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1F),
                            text = stringResource(Res.string.proxy_enabled),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.W500,
                            fontSize = 20.sp
                        )
                        Switch(
                            checked = state.enabled,
                            onCheckedChange = null
                        )
                    }
                }
                val checkedIndex = if(useCustomProxy) 1 else 0
                SettingsRadioButtonWithTitle(
                    title = stringResource(Res.string.proxy_source_builtIn),
                    checkedIndex = checkedIndex,
                    index = 0
                ) {
                    component.sendIntent(
                        SettingsProxyComponent.Intent.SetDefaultProxy
                    )
                    useCustomProxy = false
                }
                SettingsRadioButtonWithTitle(
                    title = stringResource(Res.string.proxy_source_custom),
                    checkedIndex = checkedIndex,
                    index = 1
                ) {
                    useCustomProxy = true
                }
                AnimatedVisibility(
                    visible = useCustomProxy,

                ) {
                    CustomProxyContent(
                        initialConfig = state.customProxyConfig,
                        onSave = { host, port, type, username, password ->
                            component.sendIntent(
                                SettingsProxyComponent.Intent.SetCustomProxy(
                                    host = host,
                                    port = port,
                                    type = type,
                                    username = username,
                                    password = password
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomProxyContent(
    initialConfig: ProxyConfig?,
    onSave: (
        host: String,
        port: String,
        type: Proxy.Type,
        username: String?,
        password: String?,
    ) -> Unit
) {
    var host by remember { mutableStateOf(initialConfig?.hostname ?: "") }
    var port by remember { mutableStateOf(initialConfig?.port?.toString() ?: "") }
    var typeIndex by remember {
        mutableIntStateOf(
            if(initialConfig?.type == Proxy.Type.HTTP) 0 else 1
        )
    }
    var username by remember { mutableStateOf(initialConfig?.username ?: "") }
    var password by remember { mutableStateOf(initialConfig?.password ?: "") }

    val saveButtonEnabled by remember {
        derivedStateOf {
            host.isNotBlankOrEmpty() && port.isNotBlankOrEmpty()
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = stringResource(Res.string.proxy_type_title))
        SettingsRadioButtonWithTitle(
            title = "HTTP",
            checkedIndex = typeIndex,
            index = 0,
        ) {
            typeIndex = 0
        }
        SettingsRadioButtonWithTitle(
            title = "SOCKS4/5",
            checkedIndex = typeIndex,
            index = 1,
        ) {
            typeIndex = 1
        }
        OutlinedTextField(
            value = host,
            onValueChange = { newValue -> host = newValue },
            label = { Text(text = stringResource(Res.string.proxy_hostname)) },
            singleLine = true,
            shape = CardDefaults.shape,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = port,
            onValueChange = { newValue -> port = newValue },
            label = { Text(text = stringResource(Res.string.proxy_port)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
            shape = CardDefaults.shape,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = username,
            onValueChange = { newValue -> username = newValue },
            label = { Text(text = stringResource(Res.string.proxy_username)) },
            singleLine = true,
            shape = CardDefaults.shape,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { newValue -> password = newValue },
            label = { Text(text = stringResource(Res.string.proxy_password)) },
            singleLine = true,
            shape = CardDefaults.shape,
            modifier = Modifier.fillMaxWidth(),
        )
        VerticalSpacer(4.dp)
        Button(
            onClick = {
                onSave(
                    host,
                    port,
                    if(typeIndex == 0) Proxy.Type.HTTP else Proxy.Type.SOCKS,
                    username,
                    password
                )
            },
            shape = CardShape.CardMid,
            enabled = saveButtonEnabled,
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Text(text = stringResource(Res.string.action_save))
        }
    }
}