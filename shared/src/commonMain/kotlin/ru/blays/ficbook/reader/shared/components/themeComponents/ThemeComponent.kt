package ru.blays.ficbook.reader.shared.components.themeComponents

import com.arkivanov.decompose.value.Value

interface ThemeComponent {
    val state: Value<State>

    data class State(
        val themeIndex: Int,
        val amoledTheme: Boolean,
        val dynamicColors: Boolean,
        val defaultAccentIndex: Int
    )
}