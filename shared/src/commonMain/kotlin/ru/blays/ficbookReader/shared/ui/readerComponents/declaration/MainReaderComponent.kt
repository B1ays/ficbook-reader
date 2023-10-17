package ru.blays.ficbookReader.shared.ui.readerComponents.declaration

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

interface MainReaderComponent {
    val state: Value<State>

    val dialog: Value<ChildSlot<*, SettingsReaderComponent>>

    fun onIntent(intent: Intent)

    fun onOutput(output: Output)

    sealed class Intent {
        data object OpenCloseSettings: Intent()
        data class ChangeChapter(val chapterIndex: Int): Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
    }

    @Serializable
    data class DialogConfig(val settings: Settings)

    @Serializable
    data class State(
        val chapterIndex: Int = 1,
        val chaptersCount: Int = 1,
        val chapterName: String = "",
        val text: String = "",
        val loading: Boolean = true,
        val error: Boolean = false,
        val settings: Settings = Settings()
    )

    @Serializable
    data class Settings(
        val darkColor: Int = Color.Black.toArgb(),
        val lightColor: Int = Color.White.toArgb(),
        val fontSize: Int = 17,
        val nightMode: Boolean = true,
        val fullscreenMode: Boolean = true
    )
}