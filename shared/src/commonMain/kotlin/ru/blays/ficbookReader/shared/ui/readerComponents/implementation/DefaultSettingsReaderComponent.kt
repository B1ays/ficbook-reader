package ru.blays.ficbookReader.shared.ui.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.SettingsReaderComponent

class DefaultSettingsReaderComponent(
    componentContext: ComponentContext,
    initialSettings: MainReaderComponent.Settings,
    private val onSettingsChanged: (MainReaderComponent.Settings) -> Unit
): SettingsReaderComponent, ComponentContext by componentContext {
    private val _state: MutableValue<MainReaderComponent.Settings> = MutableValue(initialSettings)
    override val state: Value<MainReaderComponent.Settings>
        get() = _state

    override fun sendIntent(intent: SettingsReaderComponent.Intent) {
        when(intent) {
            is SettingsReaderComponent.Intent.DarkColorChanged -> {
                changeState(
                    state.value.copy(
                        darkColor = intent.color
                    )
                )
            }
            is SettingsReaderComponent.Intent.LightColorChanged -> {
                changeState(
                    state.value.copy(
                        lightColor = intent.color
                    )
                )
            }
            is SettingsReaderComponent.Intent.FontSizeChanged -> {
                changeState(
                    state.value.copy(
                        fontSize = intent.fontSize
                    )
                )
            }
            is SettingsReaderComponent.Intent.FullscreenModeChanged -> {
                changeState(
                    state.value.copy(
                        fullscreenMode = intent.fullscreenMode
                    )
                )
            }
            is SettingsReaderComponent.Intent.NightModeChanged -> {
                changeState(
                    state.value.copy(
                        nightMode = intent.nightMode
                    )
                )
            }
        }
    }

    private fun changeState(newState: MainReaderComponent.Settings) {
        _state.update {
            newState
        }
        onSettingsChanged(state.value)
    }
}
