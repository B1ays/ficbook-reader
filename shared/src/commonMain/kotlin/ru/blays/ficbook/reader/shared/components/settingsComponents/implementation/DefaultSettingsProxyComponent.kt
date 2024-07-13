package ru.blays.ficbook.reader.shared.components.settingsComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsProxyComponent
import ru.blays.ficbook.reader.shared.components.snackbarStateHost.SnackbarHost
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.json.ProxyConfig
import ru.blays.ficbook.reader.shared.proxy.IProxyHolder
import ru.blays.ficbook.reader.shared.proxy.ProxyData

class DefaultSettingsProxyComponent(
    componentContext: ComponentContext,
    private val onOutput: (SettingsProxyComponent.Output) -> Unit
) : ComponentContext by componentContext, SettingsProxyComponent, KoinComponent {
    private val proxyHolder: IProxyHolder by inject()
    private val settings: ObservableSettings = get()

    private val _state = MutableStateFlow(createState())

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override val state: StateFlow<SettingsProxyComponent.State>
        get() = _state.asStateFlow()

    override fun sendIntent(intent: SettingsProxyComponent.Intent) {
        when (intent) {
            is SettingsProxyComponent.Intent.ChangeProxyEnabled -> {
                settings.putBoolean(SettingsKeys.PROXY_ENABLED_KEY, intent.enabled)
                _state.update {
                    it.copy(enabled = intent.enabled)
                }
                if (intent.enabled) {
                    val currentState = state.value
                    if (currentState.usedCustom) {
                        currentState.customProxyConfig?.let(proxyHolder::setUpConfig)
                    } else {
                        proxyHolder.setUpConfig(ProxyData.defaultSocksProxyConfig)
                    }
                } else {
                    proxyHolder.disable()
                }
            }
            is SettingsProxyComponent.Intent.SetCustomProxy -> {
                val port = intent.port.toIntOrNull() ?: return
                val config = ProxyConfig.build(
                    hostname = intent.host,
                    port = port,
                    type = intent.type
                ) {
                    intent.username?.let(::username)
                    intent.password?.let(::password)
                }
                settings.putBoolean(SettingsKeys.PROXY_USE_CUSTOM_KEY, true)
                settings.encodeValue(ProxyConfig.serializer(), SettingsKeys.PROXY_CONFIG_KEY, config)
                _state.update {
                    it.copy(
                        usedCustom = true,
                        customProxyConfig = config
                    )
                }
                if(state.value.enabled) {
                    proxyHolder.setUpConfig(config)
                }
                coroutineScope.launch {
                    SnackbarHost.showMessage(
                        message = "Прокси успешно задан"
                    )
                }
            }
            SettingsProxyComponent.Intent.SetDefaultProxy -> {
                val currentState = state.value
                if(currentState.usedCustom) {
                    settings.putBoolean(SettingsKeys.PROXY_USE_CUSTOM_KEY, false)
                    if(currentState.enabled) {
                        proxyHolder.setUpConfig(ProxyData.defaultSocksProxyConfig)
                    }
                }
            }
        }
    }

    override fun onOutput(output: SettingsProxyComponent.Output) {
        onOutput.invoke(output)
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createState(): SettingsProxyComponent.State {
        val enabled = settings.getBoolean(SettingsKeys.PROXY_ENABLED_KEY, true)
        val usedCustom = settings.getBoolean(SettingsKeys.PROXY_USE_CUSTOM_KEY, false)
        val customProxyConfig = settings.decodeValueOrNull(
            ProxyConfig.serializer(),
            SettingsKeys.PROXY_CONFIG_KEY
        )
        return SettingsProxyComponent.State(
            enabled = enabled,
            usedCustom = usedCustom,
            customProxyConfig = customProxyConfig
        )
    }
}