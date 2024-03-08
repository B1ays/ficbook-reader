package ru.blays.ficbook.reader.shared.components.commentsComponent.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.dataModels.CommentBlockModel
import ru.blays.ficbook.api.parsers.CommentParser
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.CommentBlockModelStable
import ru.blays.ficbook.reader.shared.data.mappers.toApiModel
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.ICommentsRepo
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.WriteCommentComponent
import kotlin.time.Duration.Companion.seconds

class DefaultWriteCommentComponent(
    componentContext: ComponentContext,
    private val partID: String,
    private val onCommentPosted: () -> Unit
): WriteCommentComponent, ComponentContext by componentContext {
    private val repository: ICommentsRepo by getKoin().inject()
    private val commentsParser = CommentParser()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var generateModelJob: Job? = null

    private val _state = MutableValue(
        WriteCommentComponent.State(
            text = "",
            renderedBlocks = emptyList(),
            followType = 1,
            error = false,
            errorMessage = null
        )
    )

    override val state get() = _state

    override fun editText(newText: String) {
        _state.update {
            it.copy(text = newText)
        }
        updatePreview()
    }

    override fun addReply(blocks: List<CommentBlockModelStable>) {
        coroutineScope.launch {
            val convertedBlocks = blocks.fold(StringBuilder()) { builder, block ->
                val converted = commentsParser.blockToText(
                    blockModel = block.toApiModel()
                )
                builder.append(converted)
            }.toString()
            _state.update {
                val newText = if(it.text.isNotEmpty()) {
                    "${it.text}\n$convertedBlocks"
                } else convertedBlocks
                it.copy(
                    text = newText
                )
            }
            updatePreview()
        }
    }

    override fun post() {
        coroutineScope.launch {
            val result = repository.postComment(
                partID = partID,
                text = state.value.text,
                followType = state.value.followType
            )
            when(result) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            error = true,
                            errorMessage = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    onCommentPosted()
                    _state.update {
                        it.copy(
                            text = "",
                            renderedBlocks = emptyList()
                        )
                    }
                }
            }
        }
    }

    private fun updatePreview() {
        generateModelJob?.cancel()
        generateModelJob = coroutineScope.launch {
            delay(1.seconds)
            if(generateModelJob?.isActive == true) {
                val blocks = commentsParser
                    .parseBlocks(state.value.text)
                    .map(CommentBlockModel::toStableModel)
                _state.update {
                    it.copy(renderedBlocks = blocks)
                }
            }
        }
    }
}