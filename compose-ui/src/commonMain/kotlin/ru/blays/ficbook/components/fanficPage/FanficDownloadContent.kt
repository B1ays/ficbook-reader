package ru.blays.ficbook.components.fanficPage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.darkrockstudios.libraries.mpfilepicker.FileSaver
import ficbook_reader.compose_ui.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.DownloadFanficComponent
import ru.blays.ficbook.ui_components.spacers.VerticalSpacer
import ru.blays.ficbook.utils.primaryColorAtAlpha
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@Composable
fun FanficDownloadContent(component: DownloadFanficComponent) {
    val state by component.state.subscribeAsState()

    val formatsOnSite = component.formatsOnSite
    val formatsInApp = component.formatsInApp

    Scaffold(
        topBar = {
            CollapsingToolbar(
                navigationIcon = {
                    IconButton(
                        onClick = {
                            component.sendIntent(DownloadFanficComponent.Intent.Close)
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.content_description_icon_back)
                        )
                    }
                },
                collapsingTitle = CollapsingTitle.large(stringResource(Res.string.toolbar_title_download)),
                insets = WindowInsets.statusBars
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .verticalScroll(rememberScrollState()),
        ) {
            CardWithFormats(
                title = "Скачать с сайта",
                description = "Скачать фанфик с серверов ficbook.\n" +
                        "На сайте имеется ограничение на 10 загрузок в день для пользователей без улучшенного аккаунта.",
                formats = formatsOnSite,
                onSelect = { format ->
                    component.sendIntent(
                        DownloadFanficComponent.Intent.SelectMethod(0)
                    )
                    component.sendIntent(
                        DownloadFanficComponent.Intent.PickFile(format)
                    )
                }
            )
            VerticalSpacer(12.dp)
            CardWithFormats(
                title = "Создать в приложении",
                description = "Загружает все главы с сайта и генерирует файл выбранного формата.\n" +
                        "Из-за ограничений сайта загрузка занимет много времени.",
                formats = formatsInApp,
                onSelect = { format ->
                    component.sendIntent(
                        DownloadFanficComponent.Intent.SelectMethod(1)
                    )
                    component.sendIntent(
                        DownloadFanficComponent.Intent.PickFile(format)
                    )
                }
            )
        }
    }

    state.selectedFormat?.let { extension ->
        FileSaver(
            show = state.showFilePicker,
            fileName = state.fanficName,
            fileExtension = extension
        ) { file ->
            component.sendIntent(
                DownloadFanficComponent.Intent.CloseFilePicker
            )
            if(file != null) {
                component.sendIntent(
                    DownloadFanficComponent.Intent.Download(file)
                )
            }
        }
    }
}

@Composable
private fun CardWithFormats(
    title: String,
    description: String,
    formats: Array<String>,
    onSelect: (format: String) -> Unit
) {
    var descriptionOpened by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.padding(DefaultPadding.CardDefaultPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryColorAtAlpha(0.3F)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(DefaultPadding.CardDefaultPadding)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.W400,
                    modifier = Modifier.weight(1F),
                )
                IconButton(
                    onClick = {
                        descriptionOpened = !descriptionOpened
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_info),
                        contentDescription = stringResource(Res.string.content_description_icon_info),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            AnimatedVisibility(
                visible = descriptionOpened,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            VerticalSpacer(8.dp)
            formats.forEach { format ->
                FormatItem(
                    format = format,
                    onClick = { onSelect(format) }
                )
                VerticalSpacer(6.dp)
            }
        }
    }
}

@Composable
fun FormatItem(
    format: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(0.25F)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.action_download_in_format, format),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1F)
            )
            Icon(
                painter = painterResource(Res.drawable.ic_download),
                contentDescription = stringResource(Res.string.content_description_icon_download),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}