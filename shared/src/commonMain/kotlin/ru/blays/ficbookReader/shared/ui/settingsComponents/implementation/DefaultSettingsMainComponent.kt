package ru.blays.ficbookReader.shared.ui.settingsComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbookReader.shared.ui.settingsComponents.declaration.SettingsMainComponent

class DefaultSettingsMainComponent(
    componentContext: ComponentContext,
    private val output: (SettingsMainComponent.Output) -> Unit
): SettingsMainComponent, ComponentContext by componentContext {
    override val themeSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("theme_component"),
        key = ISettingsRepository.intKey(SettingsKeys.THEME_KEY),
        defaultValue = 0
    )
    override val amoledSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("amoled_component"),
        key = ISettingsRepository.booleanKey(SettingsKeys.AMOLED_THEME_KEY),
        defaultValue = false
    )
    override val dynamicColorsSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("dynamic_colors_component"),
        key = ISettingsRepository.booleanKey(SettingsKeys.DYNAMIC_COLORS_KEY),
        defaultValue = true
    )
    override val accentIndexSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("accent_index_component"),
        key = ISettingsRepository.intKey(SettingsKeys.ACCENT_INDEX_KEY),
        defaultValue = 3
    )

    override val superfilterSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("superfilter_component"),
        key = ISettingsRepository.stringKey(SettingsKeys.SUPERFILTER_KEY),
        defaultValue = ""
    )

    override fun onOutput(output: SettingsMainComponent.Output) = this.output(output)
}