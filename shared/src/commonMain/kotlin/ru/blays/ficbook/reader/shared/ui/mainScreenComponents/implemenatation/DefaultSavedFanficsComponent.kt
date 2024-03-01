package ru.blays.ficbook.reader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import ru.blays.ficbook.reader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbook.reader.shared.ui.mainScreenComponents.declaration.SavedFanficsComponent

class DefaultSavedFanficsComponent(
    componentContext: ComponentContext,
    private val onOutput: (SavedFanficsComponent.Output) -> Unit
): SavedFanficsComponent, ComponentContext by componentContext {
    private val _state: MutableValue<List<FanficCardModelStable>> = MutableValue(emptyList())
    override val state get() = _state

    override fun onOutput(output: SavedFanficsComponent.Output) {
        onOutput.invoke(output)
    }
}