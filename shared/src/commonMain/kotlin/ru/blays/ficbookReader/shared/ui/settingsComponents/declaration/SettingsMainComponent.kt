package ru.blays.ficbookReader.shared.ui.settingsComponents.declaration

interface SettingsMainComponent {
    val themeSetting: SettingsUnitComponent<Int>
    val amoledSetting: SettingsUnitComponent<Boolean>
    val dynamicColorsSetting: SettingsUnitComponent<Boolean>
    val accentIndexSetting: SettingsUnitComponent<Int>

    fun onOutput(output: Output)

    sealed class Output {
        data object NavigateBack: Output()
    }
}