package ru.blays.ficbook.components.fanficPage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.darkrockstudios.libraries.mpfilepicker.FileSaver
import com.moriatsushi.insetsx.statusBars
import ficbook_reader.`compose-ui`.generated.resources.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.blays.ficbook.reader.shared.ui.fanficPageComponents.declaration.DownloadFanficComponent
import ru.blays.ficbook.values.DefaultPadding
import ru.hh.toolbar.custom_toolbar.CollapsingTitle
import ru.hh.toolbar.custom_toolbar.CollapsingToolbar

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FanficDownloadContent(component: DownloadFanficComponent) {
    val state by component.state.subscribeAsState()
    val formats = component.formats

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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(formats) { format ->
                FormatItem(
                    format = format,
                    onClick = {
                        component.sendIntent(DownloadFanficComponent.Intent.PickFile(format))
                    }
                )
            }
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

@OptIn(ExperimentalResourceApi::class)
@Composable
fun FormatItem(
    format: DownloadFanficComponent.FileFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(DefaultPadding.CardDefaultPadding)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.action_download_in_format, format.extension),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(12.dp)
            )
            Spacer(modifier = Modifier.weight(1F))
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_download),
                    contentDescription = stringResource(Res.string.content_description_icon_download),
                    tint = MaterialTheme.colorScheme.surfaceTint,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}