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
                _state.update {
                    it.copy(
                        darkColor = intent.color
                    )
                }
                onSettingsChanged(state.value)
            }
            is SettingsReaderComponent.Intent.LightColorChanged -> {
                _state.update {
                    it.copy(
                        lightColor = intent.color
                    )
                }
                onSettingsChanged(state.value)
            }
            is SettingsReaderComponent.Intent.FontSizeChanged -> {
                _state.update {
                    it.copy(
                        fontSize = intent.fontSize
                    )
                }
                onSettingsChanged(state.value)
            }
            is SettingsReaderComponent.Intent.FullscreenModeChanged -> {
                _state.update {
                    it.copy(
                        fullscreenMode = intent.fullscreenMode
                    )
                }
                onSettingsChanged(state.value)
            }
            is SettingsReaderComponent.Intent.NightModeChanged -> {
                _state.update {
                    it.copy(
                        nightMode = intent.nightMode
                    )
                }
                onSettingsChanged(state.value)
            }
        }
    }
}
