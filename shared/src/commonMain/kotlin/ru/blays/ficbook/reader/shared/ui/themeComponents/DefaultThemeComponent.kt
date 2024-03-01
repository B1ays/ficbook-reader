package ru.blays.ficbook.reader.shared.ui.themeComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsRepository.SettingsFlowDelegate.Companion.lastSynchronously

class DefaultThemeComponent(
    componentContext: ComponentContext
) : ThemeComponent, ComponentContext by componentContext {
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val settingsComponent: ISettingsRepository by inject(ISettingsRepository::class.java)
    private val themeCodeFlow by settingsComponent.getFlowDelegate(
        key = ISettingsRepository.intKey(SettingsKeys.THEME_KEY),
        defaultValue = 0
    )
    private val amoledThemeFlow by settingsComponent.getFlowDelegate(
        key = ISettingsRepository.booleanKey(SettingsKeys.AMOLED_THEME_KEY),
        defaultValue = false
    )
    private val accentIndexFlow by settingsComponent.getFlowDelegate(
        key = ISettingsRepository.intKey(SettingsKeys.ACCENT_INDEX_KEY),
        defaultValue = 3
    )
    private val dynamicColorsFlow by settingsComponent.getFlowDelegate(
        key = ISettingsRepository.booleanKey(SettingsKeys.DYNAMIC_COLORS_KEY),
        defaultValue = true
    )
    private val stateFlow = combine(
        themeCodeFlow,
        amoledThemeFlow,
        accentIndexFlow,
        dynamicColorsFlow
    ) { themeCode, amoledTheme, accentIndex, dynamicColors ->
        ThemeComponent.State(
            themeIndex = themeCode,
            amoledTheme = amoledTheme,
            dynamicColors = dynamicColors,
            defaultAccentIndex = accentIndex
        )
    }

    private val _state: MutableValue<ThemeComponent.State> = MutableValue(
        stateFlow.lastSynchronously()
    )
    override val state: Value<ThemeComponent.State>
        get() = _state

    init {
        coroutineScope.launch {
            stateFlow.collect { newState ->
                _state.update {
                    newState
                }
            }
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}