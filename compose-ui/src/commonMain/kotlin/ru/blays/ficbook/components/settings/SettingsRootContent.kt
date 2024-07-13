package ru.blays.ficbook.components.settings

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsRootComponent

@Composable
fun SettingsRootContent(component: SettingsRootComponent) {
    Children(
        stack = component.childStack,
    ) {
        when(
            val instance = it.instance
        ) {
            is SettingsRootComponent.Child.MainSettings -> SettingsMainContent(instance.component)
            is SettingsRootComponent.Child.ProxySettings -> SettingsProxyContent(instance.component)
        }

    }
}