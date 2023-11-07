package ru.blays.ficbookReader.shared.ui.settingsComponents.declaration

interface SettingsMainComponent {
    // Theme
    val themeSetting: SettingsUnitComponent<Int>
    val amoledSetting: SettingsUnitComponent<Boolean>
    val dynamicColorsSetting: SettingsUnitComponent<Boolean>
    val accentIndexSetting: SettingsUnitComponent<Int>

    val superfilterSetting: SettingsUnitComponent<String>
    val autoVoteSetting: SettingsUnitComponent<Boolean>

    fun onOutput(output: Output)

    sealed class Output {
        data object NavigateBack: Output()
    }
}