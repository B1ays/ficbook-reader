package ru.blays.ficbook.components.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.platformUtils.WindowSize
import ru.blays.ficbook.platformUtils.scaleContent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsMainComponent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsUnitComponent
import ru.blays.ficbook.reader.shared.data.dto.FanficDirection
import ru.blays.ficbook.reader.shared.platformUtils.blurSupported
import ru.blays.ficbook.theme.defaultAccentColors
import ru.blays.ficbook.ui_components.LazyItems.itemWithHeader
import ru.blays.ficbook.utils.LocalGlassEffectConfig
import ru.blays.ficbook.utils.thenIf
import ru.blays.ficbook.values.CardShape
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar


@OptIn(ExperimentalResourceApi::class)
@Composable
fun SettingsContent(component: SettingsMainComponent) {
    val windowSize = WindowSize()
    val widthFill = if(scaleContent) {
        when (windowSize.width) {
            in 1300..Int.MAX_VALUE -> 0.6F
            in 1000..1300 -> 0.7F
            in 800..1000 -> 0.8F
            in 600..800 -> 0.9F
            else -> 1F
        }
    } else 1F

    val blurConfig = LocalGlassEffectConfig.current
    val hazeState = remember { HazeState() }

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                SettingsMainComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.small("Настройки"),
                containerColor = if(blurConfig.blurEnabled) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface
                },
                collapsedElevation = if(blurConfig.blurEnabled) 0.dp else 4.dp,
                insets = WindowInsets.statusBars,
                modifier = Modifier.thenIf(blurConfig.blurEnabled) {
                    hazeChild(
                        state = hazeState,
                        style = blurConfig.style
                    )
                },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val themeGroupTitle = stringResource(Res.string.settings_group_theme)
            val commonGroupTitle = stringResource(Res.string.settings_group_common)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(widthFill)
                    .fillMaxHeight()
                    .thenIf(blurConfig.blurEnabled) {
                        haze(
                            state = hazeState
                        )
                    },
                contentPadding = padding,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemWithHeader(themeGroupTitle) {
                    ThemeSetting(component.themeSetting)
                    if(component.dynamicColorsSetting != null) {
                        DynamicColorsSetting(component.dynamicColorsSetting!!)
                    }
                    AmoledThemeSetting(component.amoledSetting)
                    AccentColorSetting(component.accentIndexSetting)
                    if(blurSupported) {
                        BlurSetting(
                            enabledComponent = component.glassEffectEnabled,
                            alphaComponent = component.blurAlpha,
                            radiusComponent = component.blurRadius,
                            noiseFactorComponent = component.blurNoiseFactor
                        )
                    }
                }
                itemWithHeader(commonGroupTitle) {
                    SuperfilterSetting(component.superfilterSetting)
                    AutoVoteSetting(component.autoVoteSetting)
                    TypografSetting(component.typografSetting)
                    if(component.chromeCustomTabsSetting != null) {
                        CustomTabsSetting(component.chromeCustomTabsSetting!!)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ThemeSetting(component: SettingsUnitComponent<Int>) {
    val state by component.state.collectAsState()
    val darkTheme = isSystemInDarkTheme()
    val sunIcon = painterResource(Res.drawable.ic_sun)
    val moonIcon = painterResource(Res.drawable.ic_moon)
    val icon = remember(state, darkTheme) {
        when {
            state == 0 && darkTheme -> moonIcon
            state == 0 && !darkTheme -> sunIcon
            state == 1 -> moonIcon
            else -> sunIcon
        }
    }
    SettingsExpandableCard(
        title = stringResource(Res.string.setting_title_theme),
        subtitle = stringResource(Res.string.setting_subtitle_theme),
        icon = icon,
        shape = CardShape.CardStart
    ) {
        SettingsRadioButtonWithTitle(
            title = stringResource(Res.string.theme_system),
            checkedIndex = state,
            index = 0
        ) {
            component.onIntent(
                SettingsUnitComponent.Intent.ChangeValue(it)
            )
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(Res.string.theme_dark),
            checkedIndex = state,
            index = 1
        ) {
            component.onIntent(
                SettingsUnitComponent.Intent.ChangeValue(it)
            )
        }
        SettingsRadioButtonWithTitle(
            title = stringResource(Res.string.theme_light),
            checkedIndex = state,
            index = 2
        ) {
            component.onIntent(
                SettingsUnitComponent.Intent.ChangeValue(it)
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun DynamicColorsSetting(component: SettingsUnitComponent<Boolean>) {
    val state by component.state.collectAsState()
    val icon = painterResource(Res.drawable.ic_color_swatches)
    SettingsCardWithSwitch(
        title = stringResource(Res.string.setting_title_monet),
        subtitle = stringResource(Res.string.setting_subtitle_monet),
        icon = icon,
        shape = CardShape.CardMid,
        enabled = state
    ) {
        component.onIntent(
            SettingsUnitComponent.Intent.ChangeValue(it)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AmoledThemeSetting(component: SettingsUnitComponent<Boolean>) {
    val state by component.state.collectAsState()
    val icon = painterResource(Res.drawable.ic_eclipse)
    SettingsCardWithSwitch(
        title = stringResource(Res.string.setting_title_amoled),
        subtitle = stringResource(Res.string.setting_subtitle_amoled),
        icon = icon,
        shape = CardShape.CardMid,
        enabled = state
    ) {
        component.onIntent(
            SettingsUnitComponent.Intent.ChangeValue(it)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AccentColorSetting(component: SettingsUnitComponent<Int>) {
    val state by component.state.collectAsState()
    val icon = painterResource(Res.drawable.ic_brush)
    val lazyListState = rememberLazyListState()
    SettingsExpandableCard(
        title = stringResource(Res.string.setting_title_accent),
        subtitle = stringResource(Res.string.setting_subtitle_accent),
        icon = icon,
        shape = CardShape.CardMid,
    ) {
        LazyRow(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPaddingSmall)
                .fillMaxWidth(),
            state = lazyListState
        ) {
            itemsIndexed(defaultAccentColors) { index, color ->
                ColorPickerItem(
                    color = color,
                    index = index,
                    selectedItemIndex = state,
                    actionSelectColor = { newValue ->
                        component.onIntent(
                            SettingsUnitComponent.Intent.ChangeValue(newValue)
                        )
                    }

                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun SuperfilterSetting(component: SettingsUnitComponent<String>) {
    val state by component.state.collectAsState()
    val icon = painterResource(Res.drawable.ic_filter_outlined)
    val selectedDirections: List<FanficDirection> = remember(state) {
        val directionNames = state
            .removeSuffix(",")
            .split(",")

        directionNames.map(FanficDirection::getForName)
    }

    fun newValue(
        direction: FanficDirection,
        add: Boolean,
        list: List<FanficDirection>
    ): String {
        return if (add) {
            list + direction
        } else {
            list.filterNot { it == direction }
        }.joinToString("") { it.name + ',' }
    }

    SettingsExpandableCard(
        title = stringResource(Res.string.setting_title_superfilter),
        subtitle = stringResource(Res.string.setting_subtitle_superfilter),
        icon = icon,
        shape = CardShape.CardStart,
    ) {
        FanficDirection.entries
            .filterNot { it == FanficDirection.UNKNOWN }
            .forEach { direction ->
                SettingsCheckboxWithTitle(
                    title = direction.direction,
                    state = direction in selectedDirections
                ) { add ->
                    val newValue = newValue(
                        direction = direction,
                        add = add,
                        list = selectedDirections
                    )
                    component.onIntent(
                        SettingsUnitComponent.Intent.ChangeValue(
                            value = newValue
                        )
                    )
                }
            }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AutoVoteSetting(component: SettingsUnitComponent<Boolean>) {
    val state by component.state.collectAsState()
    SettingsCardWithSwitch(
        title = stringResource(Res.string.setting_title_auto_vote),
        subtitle = stringResource(Res.string.setting_subtitle_auto_vote),
        enabled = state,
        icon = painterResource(Res.drawable.ic_vote),
        shape = CardShape.CardMid,
    ) { newValue ->
        component.onIntent(
            SettingsUnitComponent.Intent.ChangeValue(newValue)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun CustomTabsSetting(component: SettingsUnitComponent<Boolean>) {
    val state by component.state.collectAsState()
    SettingsCardWithSwitch(
        title = stringResource(Res.string.setting_title_custom_tabs),
        subtitle = stringResource(Res.string.setting_subtitle_custom_tabs),
        enabled = state,
        icon = painterResource(Res.drawable.ic_chrome),
        shape = CardShape.CardEnd,
    ) { newValue ->
        component.onIntent(
            SettingsUnitComponent.Intent.ChangeValue(newValue)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TypografSetting(component: SettingsUnitComponent<Boolean>) {
    val state by component.state.collectAsState()
    SettingsCardWithSwitch(
        title = stringResource(Res.string.setting_title_typograf),
        subtitle = stringResource(Res.string.setting_subtitle_typograf),
        icon = painterResource(Res.drawable.ic_magic_wand),
        shape = CardShape.CardMid,
        enabled = state
    ) { newValue ->
        component.onIntent(
            SettingsUnitComponent.Intent.ChangeValue(newValue)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BlurSetting(
    enabledComponent: SettingsUnitComponent<Boolean>,
    alphaComponent: SettingsUnitComponent<Float>,
    radiusComponent: SettingsUnitComponent<Float>,
    noiseFactorComponent: SettingsUnitComponent<Float>
) {
    SettingsExpandableCard(
        title = stringResource(Res.string.setting_title_glassmorphism),
        subtitle = stringResource(Res.string.setting_subtitle_glassmorphism),
        icon = painterResource(Res.drawable.ic_blur),
        shape = CardShape.CardEnd,
    ) {
        val enabled by enabledComponent.state.collectAsState()

        SettingsSwitchWithTitle(
            title = stringResource(Res.string.enabled),
            state = enabled,
            action = { newValue ->
                enabledComponent.onIntent(
                    SettingsUnitComponent.Intent.ChangeValue(newValue)
                )
            }
        )
        Spacer(modifier = Modifier.height(3.dp))
        SettingsSliderWithTitle(
            title = stringResource(Res.string.setting_subtitle_glassmorphism_alpha),
            enabled = enabled,
            value = alphaComponent.state.collectAsState().value,
            valueRange = 0F..1F,
            onValueChange = { newValue ->
                alphaComponent.onIntent(
                    SettingsUnitComponent.Intent.ChangeValue(newValue)
                )
            }
        )
        SettingsSliderWithTitle(
            title = stringResource(Res.string.setting_subtitle_glassmorphism_radius),
            enabled = enabled,
            value = radiusComponent.state.collectAsState().value,
            valueRange = 5F..40F,
            onValueChange = { newValue ->
                radiusComponent.onIntent(
                    SettingsUnitComponent.Intent.ChangeValue(newValue)
                )
            }
        )
        SettingsSliderWithTitle(
            title = stringResource(Res.string.setting_subtitle_glassmorphism_noise),
            enabled = enabled,
            value = noiseFactorComponent.state.collectAsState().value,
            valueRange = 0F..1F,
            onValueChange = { newValue ->
                noiseFactorComponent.onIntent(
                    SettingsUnitComponent.Intent.ChangeValue(newValue)
                )
            }
        )
    }
}