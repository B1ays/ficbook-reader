package ru.blays.ficbookReader.shared.ui.readerComponents.declaration

import com.arkivanov.decompose.value.Value

interface SettingsReaderComponent {
    val state: Value<MainReaderComponent.Settings>

    fun sendIntent(intent: Intent)

    sealed class Intent {
        data class DarkColorChanged(val color: Int): Intent()
        data class LightColorChanged(val color: Int): Intent()
        data class FontSizeChanged(val fontSize: Int): Intent()
        data class NightModeChanged(val nightMode: Boolean): Intent()
        data class FullscreenModeChanged(val fullscreenMode: Boolean): Intent()
        data class ScrollWithVolumeKeysChanged(val scrollWithVolumeButtons: Boolean): Intent()
        data class KeepScreenOnChanged(val keepScreenOn: Boolean): Intent()

    }
}