package ru.blays.ficbook.components.settingsContent

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import ru.blays.ficbook.components.superfilterContent.SuperfilterRootContent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsRootComponent
import ru.blays.ficbook.utils.defaultPredictiveAnimation

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun SettingsRootContent(component: SettingsRootComponent) {
    Children(
        stack = component.childStack,
        animation = defaultPredictiveAnimation(
            backHandler = component.backHandler,
            onBack = component::onNavigateBack
        )
    ) {
        when(
            val child = it.instance
        ) {
            is SettingsRootComponent.Child.MainSettings -> SettingsMainContent(child.component)
            is SettingsRootComponent.Child.ProxySettings -> SettingsProxyContent(child.component)
            is SettingsRootComponent.Child.Superfilter -> SuperfilterRootContent(child.component)
        }
    }
}