package ru.blays.ficbook.reader.shared.components.settingsComponents.declaration

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbook.reader.shared.preferences.json.ProxyConfig
import java.net.Proxy

interface SettingsProxyComponent {
    val state: StateFlow<State>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    data class State(
        val enabled: Boolean,
        val usedCustom: Boolean,
        val customProxyConfig: ProxyConfig?
    )

    sealed class Intent {
        data class ChangeProxyEnabled(val enabled: Boolean): Intent()
        data class SetCustomProxy(
            val host: String,
            val port: String,
            val type: Proxy.Type,
            val username: String?,
            val password: String?
        ): Intent()
        data object SetDefaultProxy: Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
    }
}