package ru.blays.ficbook.components.fanficPage.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.godaddy.android.colorpicker.toColorInt
import ficbook_reader.compose_ui.generated.resources.Res
import ficbook_reader.compose_ui.generated.resources.ic_dropper
import org.jetbrains.compose.resources.painterResource
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.MainReaderComponent
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.SettingsReaderComponent
import ru.blays.ficbook.ui_components.CustomButton.CustomIconButton
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.values.DefaultPadding

@Suppress("DEPRECATION")
@Composable
fun ReaderSettingPopup(
    component: SettingsReaderComponent,
    readerSettingsModel: MainReaderComponent.Settings,
    closeDialog: () -> Unit
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = {}
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9F)
        ) {
            Column(
                modifier = Modifier
                    .padding(DefaultPadding.CardDefaultPaddingLarge)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Настройки читалки",
                    style = MaterialTheme.typography.titleLarge
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ночная тема",
                        modifier = Modifier.fillMaxWidth(0.7F)
                    )
                    Switch(
                        checked = readerSettingsModel.nightMode,
                        onCheckedChange = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.NightModeChanged(it)
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Полноэкранный режим",
                        modifier = Modifier.fillMaxWidth(0.7F)
                    )
                    Switch(
                        checked = readerSettingsModel.fullscreenMode,
                        onCheckedChange = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.FullscreenModeChanged(it)
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Перелистывание кнопками громкости",
                        modifier = Modifier.fillMaxWidth(0.7F)
                    )
                    Switch(
                        checked = readerSettingsModel.scrollWithVolumeButtons,
                        onCheckedChange = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.ScrollWithVolumeKeysChanged(it)
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Не выключать экран",
                        modifier = Modifier.fillMaxWidth(0.7F)
                    )
                    Switch(
                        checked = readerSettingsModel.keepScreenOn,
                        onCheckedChange = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.KeepScreenOnChanged(it)
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Автоматически переходить к следующей главе",
                        modifier = Modifier.fillMaxWidth(0.7F)
                    )
                    Switch(
                        checked = readerSettingsModel.autoOpenNextChapter,
                        onCheckedChange = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.AutoOpenNextChapterChanged(it)
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Размер шрифта",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomIconButton(
                        shape = CircleShape,
                        contentColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        minSize = 30.dp,
                        onClick = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.FontSizeChanged(
                                    (readerSettingsModel.fontSize - 1).coerceAtLeast(1)
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = readerSettingsModel.fontSize.toString())
                    Spacer(modifier = Modifier.width(6.dp))
                    CustomIconButton(
                        shape = CircleShape,
                        contentColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        minSize = 30.dp,
                        onClick = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.FontSizeChanged(
                                    (readerSettingsModel.fontSize + 1)
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Высота строки",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomIconButton(
                        shape = CircleShape,
                        contentColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        minSize = 30.dp,
                        onClick = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.LineHeightChanged(
                                    readerSettingsModel.lineHeight.minus(1).coerceAtLeast(1)
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = readerSettingsModel.lineHeight.toString())
                    Spacer(modifier = Modifier.width(6.dp))
                    CustomIconButton(
                        shape = CircleShape,
                        contentColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        minSize = 30.dp,
                        onClick = {
                            component.sendIntent(
                                SettingsReaderComponent.Intent.LineHeightChanged(
                                    readerSettingsModel.lineHeight.plus(1).coerceAtMost(30)
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                val lightColor = remember(readerSettingsModel.lightColor) { Color(readerSettingsModel.lightColor) }
                var lightColorSelectorOpen by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        lightColorSelectorOpen = !lightColorSelectorOpen
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                color = lightColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Светлый цвет")
                }
                ColorPickerItem(
                    visible = lightColorSelectorOpen,
                    initialColor = lightColor,
                    onColorSelected = { colorArgb ->
                        component.sendIntent(
                            SettingsReaderComponent.Intent.LightColorChanged(colorArgb)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(6.dp))
                var darkColorSelectorOpen by remember { mutableStateOf(false) }
                val darkColor = remember(readerSettingsModel.darkColor) { Color(readerSettingsModel.darkColor) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        darkColorSelectorOpen = !darkColorSelectorOpen
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                darkColor,
                                RoundedCornerShape(10.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Тёмный цвет")
                }
                ColorPickerItem(
                    visible = darkColorSelectorOpen,
                    initialColor = darkColor,
                    onColorSelected = { colorArgb ->
                        component.sendIntent(
                            SettingsReaderComponent.Intent.DarkColorChanged(colorArgb)
                        )
                    }
                )
                VerticalSpacer(12.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = closeDialog
                    ) {
                        Text(text = "Закрыть")
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPickerItem(
    visible: Boolean,
    initialColor: Color,
    onColorSelected: (colorArgb: Int) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        var newColor by remember { mutableStateOf(HsvColor.from(initialColor)) }
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            ClassicColorPicker(
                color = newColor,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
            ) { hsvColor ->
                newColor = hsvColor
            }
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = {
                    onColorSelected(newColor.toColorInt())
                }
            ) {
                Text("Выбрать")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(Res.drawable.ic_dropper),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}