package ru.blays.ficbookReader.components.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.myapplication.compose.Res
import com.moriatsushi.insetsx.systemBarsPadding
import ru.blays.ficbookReader.platformUtils.WindowSize
import ru.blays.ficbookReader.shared.data.dto.FanficDirection
import ru.blays.ficbookReader.shared.ui.settingsComponents.declaration.SettingsMainComponent
import ru.blays.ficbookReader.shared.ui.settingsComponents.declaration.SettingsUnitComponent
import ru.blays.ficbookReader.theme.defaultAccentColorsList
import ru.blays.ficbookReader.ui_components.LazyItems.itemsGroupWithHeader
import ru.blays.ficbookReader.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingsToolbar

@Composable
fun SettingsContent(component: SettingsMainComponent) {
    val windowSize = WindowSize()
    val widthFill = remember(windowSize.width) {
        when (windowSize.width) {
            in 1300..Int.MAX_VALUE -> 0.6F
            in 1000..1300 -> 0.7F
            in 800..1000 -> 0.8F
            in 600..800 -> 0.9F
            else -> 1F
        }
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            CollapsingsToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.onOutput(
                                SettingsMainComponent.Output.NavigateBack
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.image.ic_arrow_back),
                            contentDescription = "Стрелка назад"
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large("Настройки")
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(widthFill)
                    .fillMaxHeight()
                    .padding(DefaultPadding.CardDefaultPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsGroupWithHeader("Тема и цвета") {
                    ThemeSetting(component.themeSetting)
                    DynamicColorsSetting(component.dynamicColorsSetting)
                    AmoledThemeSetting(component.amoledSetting)
                    AccentColorSetting(component.accentIndexSetting)
                }
                itemsGroupWithHeader("Общие") {
                    SuperfilterSetting(component.superfilterSetting)
                    AutoVoteSetting(component.autoVoteSetting)
                }
            }
        }
    }
}

@Composable
private fun ThemeSetting(component: SettingsUnitComponent<Int>) {
    val state by component.state.collectAsState()
    val darkTheme = isSystemInDarkTheme()
    val sunIcon = painterResource(Res.image.ic_sun)
    val moonIcon = painterResource(Res.image.ic_moon)
    val icon = remember(state, darkTheme) {
        when {
            state == 0 && darkTheme -> moonIcon
            state == 0 && !darkTheme -> sunIcon
            state == 1 -> moonIcon
            else -> sunIcon
        }
    }
    SettingsExpandableCard(
        title = "Тема",
        subtitle = "Выбрать тему приложения",
        icon = icon
    ) {
        SettingsRadioButtonWithTitle(
            title = "Системная тема",
            checkedIndex = state,
            index = 0
        ) {
            component.onIntent(
                SettingsUnitComponent.Intent.ChangeValue(it)
            )
        }
        SettingsRadioButtonWithTitle(
            title = "Тёмная тема",
            checkedIndex = state,
            index = 1
        ) {
            component.onIntent(
                SettingsUnitComponent.Intent.ChangeValue(it)
            )
        }
        SettingsRadioButtonWithTitle(
            title = "Светлая тема",
            checkedIndex = state,
            index = 2
        ) {
            component.onIntent(
                SettingsUnitComponent.Intent.ChangeValue(it)
            )
        }
    }
}

@Composable
private fun DynamicColorsSetting(component: SettingsUnitComponent<Boolean>) {
    val state by component.state.collectAsState()
    val icon = painterResource(Res.image.color_swatches)
    SettingsCardWithSwitch(
        title = "Monet цвета",
        subtitle = "Использовать тему из Monet",
        icon = icon,
        state = state
    ) {
        component.onIntent(
            SettingsUnitComponent.Intent.ChangeValue(it)
        )
    }
}

@Composable
private fun AmoledThemeSetting(component: SettingsUnitComponent<Boolean>) {
    val state by component.state.collectAsState()
    val icon = painterResource(Res.image.ic_eclipse)
    SettingsCardWithSwitch(
        title = "Amoled тема",
        subtitle = "Использовать Amoled тему",
        icon = icon,
        state = state
    ) {
        component.onIntent(
            SettingsUnitComponent.Intent.ChangeValue(it)
        )
    }
}

@Composable
private fun AccentColorSetting(component: SettingsUnitComponent<Int>) {
    val state by component.state.collectAsState()
    val icon = painterResource(Res.image.ic_brush)
    val lazyListState = rememberLazyListState()
    SettingsExpandableCard(
        title = "Цвет акцента",
        subtitle = "Выбрать цвет акцента",
        icon = icon
    ) {
        LazyRow(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPaddingSmall)
                .fillMaxWidth(),
            state = lazyListState
        ) {
            itemsIndexed(defaultAccentColorsList) { index, color ->
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

@Composable
private fun SuperfilterSetting(component: SettingsUnitComponent<String>) {
    val state by component.state.collectAsState()
    val icon = painterResource(Res.image.ic_filter_outlined)
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
        title = "Суперфильтр фанфиков",
        subtitle = "Скрыть выбранные направленности",
        icon = icon
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

@Composable
private fun AutoVoteSetting(component: SettingsUnitComponent<Boolean>) {
    val state by component.state.collectAsState()
    SettingsCardWithSwitch(
        title = "Авто-голосование",
        subtitle = "Автоматически ставить \"Жду продолжения\"",
        state = state,
        icon = painterResource(Res.image.ic_vote)
    ) { newValue ->
        component.onIntent(
            SettingsUnitComponent.Intent.ChangeValue(newValue)
        )
    }
}