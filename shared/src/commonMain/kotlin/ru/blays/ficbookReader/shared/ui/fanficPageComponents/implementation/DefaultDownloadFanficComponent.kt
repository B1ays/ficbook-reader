package ru.blays.ficbookReader.shared.ui.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.blays.ficbook.feature.fileDownload.downloadMPFile
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.DownloadFanficComponent
import ru.blays.ficbookapi.UrlProcessor.getUrlForHref

internal class DefaultDownloadFanficComponent(
    componentContext: ComponentContext,
    private val fanficID: String,
    private val fanficName: String,
    private val close: () -> Unit
): DownloadFanficComponent, ComponentContext by componentContext {
    private val _state = MutableValue(
        DownloadFanficComponent.State(
            showFilePicker = false,
            fanficName = fanficName,
            selectedFormat = null
        )
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val formats = getDefaultFormats()

    override val state get() = _state


    override fun sendIntent(intent: DownloadFanficComponent.Intent) {
        when(intent) {
            is DownloadFanficComponent.Intent.Close -> close()
            is DownloadFanficComponent.Intent.PickFile -> {
                _state.update {
                    it.copy(
                        showFilePicker = true,
                        selectedFormat = intent.format.extension
                    )
                }
            }
            is DownloadFanficComponent.Intent.Download -> {
                coroutineScope.launch {
                    val url = state.value.selectedFormat?.let(::buildUrl) ?: return@launch
                    downloadMPFile(
                        url = url,
                        file = intent.file
                    )
                }
                close()
            }
            DownloadFanficComponent.Intent.CloseFilePicker -> {
                _state.update {
                    it.copy(showFilePicker = false)
                }
            }
        }
    }

    private fun getDefaultFormats(): Array<DownloadFanficComponent.FileFormat> {
        return arrayOf(
            DownloadFanficComponent.FileFormat(
                "txt",
                "TXT - это обычный текстовый файл, его открывают любые программы, работающие с текстом, например, блокнот, notepad, word и другие.",
            ),
            DownloadFanficComponent.FileFormat(
                "epub",
                "EPUB - это открытый формат для электронных книг. Текст в этом формате автоматически адаптируется к размеру экрана смартфона, ноутбука или устройства для чтения электронных книг."
            ),
            DownloadFanficComponent.FileFormat(
                "pdf",
                "PDF - межплатформенный открытый формат электронных документов. Предназначен для представления полиграфической продукции в электронном виде."
            ),
            DownloadFanficComponent.FileFormat(
                "fb2",
                "FB2 - формат электронных книг, который поддерживается большим количеством программ и электронных \"читалок\"."
            )
        )
    }

    private fun buildUrl(format: String): String {
        return getUrlForHref("/fanfic_download/$fanficID/$format")
    }
}

