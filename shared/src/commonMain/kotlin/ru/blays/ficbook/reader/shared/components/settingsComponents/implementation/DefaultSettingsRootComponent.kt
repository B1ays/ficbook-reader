package ru.blays.ficbook.reader.shared.components.settingsComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsMainComponent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsProxyComponent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsRootComponent

class DefaultSettingsRootComponent(
    componentContext: ComponentContext,
    private val onOutput: (SettingsRootComponent.Output) -> Unit
): ComponentContext by componentContext, SettingsRootComponent {
    private val navigation = StackNavigation<SettingsRootComponent.Configuration>()

    override val childStack = childStack(
        source = navigation,
        initialConfiguration = SettingsRootComponent.Configuration.MainSettings,
        handleBackButton = true,
        serializer = SettingsRootComponent.Configuration.serializer(),
        childFactory = ::childFactory
    )

    private fun childFactory(
        configuration: SettingsRootComponent.Configuration,
        childContext: ComponentContext,
    ): SettingsRootComponent.Child {
        return when(configuration) {
            SettingsRootComponent.Configuration.MainSettings -> SettingsRootComponent.Child.MainSettings(
                DefaultSettingsMainComponent(
                    componentContext = childContext,
                    output = ::onMainSettingsOutput
                )
            )
            SettingsRootComponent.Configuration.ProxySettings -> SettingsRootComponent.Child.ProxySettings(
                DefaultSettingsProxyComponent(
                    componentContext = childContext,
                    onOutput = ::onProxySettingsOutput
                )
            )
        }
    }

    private fun onMainSettingsOutput(output: SettingsMainComponent.Output) {
        when(output) {
            SettingsMainComponent.Output.NavigateBack -> {
                onOutput.invoke(SettingsRootComponent.Output.NavigateBack)
            }
            SettingsMainComponent.Output.ProxySettings -> {
                navigation.push(SettingsRootComponent.Configuration.ProxySettings)
            }
        }
    }
    private fun onProxySettingsOutput(output: SettingsProxyComponent.Output) {
        when(output) {
            SettingsProxyComponent.Output.NavigateBack -> navigation.pop()
        }
    }
}