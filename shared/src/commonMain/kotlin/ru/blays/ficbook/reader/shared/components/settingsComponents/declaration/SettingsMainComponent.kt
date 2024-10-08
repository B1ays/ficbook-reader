package ru.blays.ficbook.reader.shared.components.settingsComponents.declaration

interface SettingsMainComponent {
    // Theme
    val themeSetting: SettingsUnitComponent<Int>
    val amoledSetting: SettingsUnitComponent<Boolean>
    val dynamicColorsSetting: SettingsUnitComponent<Boolean>?
    val accentIndexSetting: SettingsUnitComponent<Int>

    // Glass effect settings
    val glassEffectEnabled: SettingsUnitComponent<Boolean>
    val blurAlpha: SettingsUnitComponent<Float>
    val blurRadius: SettingsUnitComponent<Float>
    val blurNoiseFactor: SettingsUnitComponent<Float>

    val autoVoteSetting: SettingsUnitComponent<Boolean>
    val chromeCustomTabsSetting: SettingsUnitComponent<Boolean>?
    val typografSetting: SettingsUnitComponent<Boolean>

    fun onOutput(output: Output)

    sealed class Output {
        data object NavigateBack: Output()
        data object ProxySettings: Output()
        data object Superfilter: Output()
    }
}