package ru.blays.ficbook.reader.shared.components.settingsComponents.declaration

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface SettingsRootComponent {
    val childStack: Value<ChildStack<Configuration, Child>>

    sealed class Output {
        data object NavigateBack: Output()
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object MainSettings: Configuration()
        @Serializable
        data object ProxySettings: Configuration()
    }
    sealed class Child {
        data class MainSettings(val component: SettingsMainComponent): Child()
        data class ProxySettings(val component: SettingsProxyComponent): Child()
    }
}