package ru.blays.ficbook.reader.shared.components.settingsComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsMainComponent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsProxyComponent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsRootComponent
import ru.blays.ficbook.reader.shared.components.superfilterComponents.DefaultSuperfilterComponent
import ru.blays.ficbook.reader.shared.components.superfilterComponents.SuperfilterComponent

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

    override fun onNavigateBack() {
        navigation.pop()
    }

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
            SettingsRootComponent.Configuration.Superfilter -> SettingsRootComponent.Child.Superfilter(
                DefaultSuperfilterComponent(
                    componentContext = childContext,
                    onOutput = ::onSuperfilterOutput
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
                navigation.pushNew(SettingsRootComponent.Configuration.ProxySettings)
            }
            SettingsMainComponent.Output.Superfilter -> {
                navigation.pushNew(SettingsRootComponent.Configuration.Superfilter)
            }
        }
    }
    private fun onProxySettingsOutput(output: SettingsProxyComponent.Output) {
        when(output) {
            SettingsProxyComponent.Output.NavigateBack -> navigation.pop()
        }
    }

    private fun onSuperfilterOutput(output: SuperfilterComponent.Output) {
        when(output) {
            SuperfilterComponent.Output.NavigateBack -> navigation.pop()
        }
    }
}