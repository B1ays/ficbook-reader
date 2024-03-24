package ru.blays.ficbook.reader.shared.components.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.blays.ficbook.api.UrlProcessor.getUrlForHref
import ru.blays.ficbook.reader.feature.fanficDownloader.DownloadTask
import ru.blays.ficbook.reader.feature.fanficDownloader.downloadFanficInEpub
import ru.blays.ficbook.reader.feature.fileDownload.downloadMPFile
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.DownloadFanficComponent

internal class DefaultDownloadFanficComponent(
    componentContext: ComponentContext,
    private val fanficID: String,
    private val fanficName: String,
    private val onClose: () -> Unit
): DownloadFanficComponent, ComponentContext by componentContext {
    private val _state = MutableValue(
        DownloadFanficComponent.State(
            showFilePicker = false,
            fanficName = fanficName,
            selectedFormat = null
        )
    )

    private var downloadMethod: Int = 0

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val formatsOnSite = getSiteFormats()
    override val formatsInApp = getAppFormats()

    override val state get() = _state


    override fun sendIntent(intent: DownloadFanficComponent.Intent) {
        when(intent) {
            is DownloadFanficComponent.Intent.Close -> onClose()
            is DownloadFanficComponent.Intent.PickFile -> {
                _state.update {
                    it.copy(
                        showFilePicker = true,
                        selectedFormat = intent.format
                    )
                }
            }
            is DownloadFanficComponent.Intent.Download -> {
                coroutineScope.launch {
                    when(downloadMethod) {
                        0 -> {
                            val url = state.value.selectedFormat?.let(::buildUrl)
                            if(url != null) {
                                downloadMPFile(
                                    url = url,
                                    file = intent.file
                                )
                            }
                        }
                        1 -> {
                            val selectedFormat = state.value.selectedFormat
                            if(selectedFormat != null) {
                                downloadFanficInEpub(
                                    file = intent.file,
                                    task = DownloadTask(
                                        fanficID = fanficID,
                                        title = fanficName,
                                        format = selectedFormat
                                    )
                                )
                            }
                        }
                    }
                }
                onClose()
            }
            is DownloadFanficComponent.Intent.CloseFilePicker -> {
                _state.update {
                    it.copy(showFilePicker = false)
                }
            }
            is DownloadFanficComponent.Intent.SelectMethod -> {
                downloadMethod = intent.method
            }
        }
    }

    private fun getSiteFormats(): Array<String> = arrayOf(
        "txt",
        "epub",
        "pdf",
        "fb2"
    )

    private fun getAppFormats(): Array<String> = arrayOf(
        "txt",
        "epub",
    )

    private fun buildUrl(format: String): String {
        return getUrlForHref("/fanfic_download/$fanficID/$format")
    }
}

