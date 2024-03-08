package ru.blays.ficbook.reader.shared.components.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.SettingsReaderComponent

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
            is SettingsReaderComponent.Intent.KeepScreenOnChanged -> {
                changeState(
                    state.value.copy(
                        keepScreenOn = intent.keepScreenOn
                    )
                )
            }
            is SettingsReaderComponent.Intent.ScrollWithVolumeKeysChanged -> {
                changeState(
                    state.value.copy(
                        scrollWithVolumeButtons = intent.scrollWithVolumeButtons
                    )
                )
            }
            is SettingsReaderComponent.Intent.AutoOpenNextChapterChanged -> {
                changeState(
                    state.value.copy(
                        autoOpenNextChapter = intent.autoOpenNextChapter
                    )
                )
            }
            is SettingsReaderComponent.Intent.LineHeightChanged -> {
                changeState(
                    state.value.copy(
                        lineHeight = intent.lineHeight
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
