package ru.blays.ficbookReader.shared.ui.settingsComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import ru.blays.ficbookReader.shared.platformUtils.customTabsSupported
import ru.blays.ficbookReader.shared.platformUtils.dynamicColorSupported
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.json.GlassEffectConfig
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbookReader.shared.ui.settingsComponents.declaration.SettingsMainComponent
import ru.blays.ficbookReader.shared.ui.settingsComponents.declaration.SettingsUnitComponent

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
    override val dynamicColorsSetting = if(dynamicColorSupported) {
        DefaultSettingsUnitComponent(
            componentContext = childContext("dynamic_colors_component"),
            key = ISettingsRepository.booleanKey(SettingsKeys.DYNAMIC_COLORS_KEY),
            defaultValue = true
        )
    } else {
        null
    }
    override val accentIndexSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("accent_index_component"),
        key = ISettingsRepository.intKey(SettingsKeys.ACCENT_INDEX_KEY),
        defaultValue = 3
    )

    // Glass effect settings
    override val glassEffectEnabled = DefaultSettingsUnitComponent(
        componentContext = childContext(SettingsKeys.GLASS_EFFECT_ENABLED_KEY),
        key = ISettingsRepository.booleanKey(SettingsKeys.GLASS_EFFECT_ENABLED_KEY),
        defaultValue = GlassEffectConfig.DEFAULT.enabled
    )
    override val blurAlpha = DefaultSettingsUnitComponent(
        componentContext = childContext(SettingsKeys.GLASS_EFFECT_ALPHA_KEY),
        key = ISettingsRepository.floatKey(SettingsKeys.GLASS_EFFECT_ALPHA_KEY),
        defaultValue = GlassEffectConfig.DEFAULT.alpha
    )
    override val blurRadius = DefaultSettingsUnitComponent(
        componentContext = childContext(SettingsKeys.GLASS_EFFECT_RADIUS_KEY),
        key = ISettingsRepository.floatKey(SettingsKeys.GLASS_EFFECT_RADIUS_KEY),
        defaultValue = GlassEffectConfig.DEFAULT.blurRadius
    )
    override val blurNoiseFactor = DefaultSettingsUnitComponent(
        componentContext = childContext(SettingsKeys.GLASS_EFFECT_NOISE_FACTOR_KEY),
        key = ISettingsRepository.floatKey(SettingsKeys.GLASS_EFFECT_NOISE_FACTOR_KEY),
        defaultValue = GlassEffectConfig.DEFAULT.noiseFactor
    )

    override val superfilterSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("superfilter_component"),
        key = ISettingsRepository.stringKey(SettingsKeys.SUPERFILTER_KEY),
        defaultValue = ""
    )
    override val autoVoteSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("auto_vote_component"),
        key = ISettingsRepository.booleanKey(SettingsKeys.AUTO_VOTE_FOR_CONTINUE),
        defaultValue = false
    )
    override val chromeCustomTabsSetting: SettingsUnitComponent<Boolean>? = if(customTabsSupported) {
        DefaultSettingsUnitComponent(
            componentContext = childContext("chrome_custom_tabs_component"),
            key = ISettingsRepository.booleanKey(SettingsKeys.CHROME_CUSTOM_TABS_KEY),
            defaultValue = false
        )
    } else {
        null
    }
    override val typografSetting = DefaultSettingsUnitComponent(
        componentContext = childContext("typograf_component"),
        key = ISettingsRepository.booleanKey(SettingsKeys.TYPOGRAF_KEY),
        defaultValue = true
    )


    override fun onOutput(output: SettingsMainComponent.Output) = this.output(output)
}