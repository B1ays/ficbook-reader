package ru.blays.ficbook.reader.shared.components.superfilterComponents

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value

interface SuperfilterComponent {
    val pages: Value<ChildPages<Int, SuperfilterTabComponent>>

    fun changeTab(index: Int)
    fun onOutput(output: Output)

    sealed class Output {
        data object NavigateBack: Output()
    }
}