package ru.blays.ficbook.reader.shared.components.settingsComponents.declaration

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.components.superfilterComponents.SuperfilterComponent

interface SettingsRootComponent: BackHandlerOwner {
    val childStack: Value<ChildStack<Configuration, Child>>

    fun onNavigateBack()

    sealed class Output {
        data object NavigateBack: Output()
    }

    @Serializable
    sealed class Configuration {
        @Serializable
        data object MainSettings: Configuration()
        @Serializable
        data object ProxySettings: Configuration()
        @Serializable
        data object Superfilter: Configuration()
    }
    sealed class Child {
        data class MainSettings(val component: SettingsMainComponent): Child()
        data class ProxySettings(val component: SettingsProxyComponent): Child()
        data class Superfilter(val component: SuperfilterComponent): Child()
    }
}