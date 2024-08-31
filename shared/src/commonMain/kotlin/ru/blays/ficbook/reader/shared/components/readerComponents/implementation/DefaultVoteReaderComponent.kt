package ru.blays.ficbook.reader.shared.components.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.russhwolf.settings.boolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.settings
import ru.blays.ficbook.reader.shared.components.readerComponents.declaration.VoteReaderComponent

class DefaultVoteReaderComponent(
    componentContext: ComponentContext,
    private val chapters: FanficChapterStable,
    private val fanficID: String,
    private val lastChapter: () -> Boolean
): VoteReaderComponent, ComponentContext by componentContext, KoinComponent {
    private val fanficPageRepo: IFanficPageRepo by inject()

    private val _state = MutableValue(calculateState())
    override val state: Value<VoteReaderComponent.State>
        get() = _state

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val autoVoteSetting by settings.boolean(
        key = SettingsKeys.AUTO_VOTE_FOR_CONTINUE,
        defaultValue = false
    )

    override fun sendIntent(intent: VoteReaderComponent.Intent) {
        when(intent) {
            is VoteReaderComponent.Intent.Read -> coroutineScope.launch {
                setRead(intent.read)
            }
            is VoteReaderComponent.Intent.VoteForContinue -> coroutineScope.launch {
                setVote(intent.vote)
            }
        }
    }

    private suspend fun setVote(vote: Boolean) {
        if(state.value.canVote) {
            val lastChapterHref = getLastChapterHref()
            val chapterID = getChapterID(lastChapterHref)
            val success = fanficPageRepo.vote(
                vote = vote,
                partID = chapterID
            )
            if (success) {
                _state.update {
                    it.copy(
                        votedForContinue = vote
                    )
                }
            }
        }
    }

    private suspend  fun setRead(read: Boolean) {
        val success = fanficPageRepo.read(
            read = read,
            fanficID = fanficID
        )
        if (success) {
            _state.update {
                it.copy(
                    readed = read
                )
            }
        }
    }

    private fun calculateState(): VoteReaderComponent.State {
        val canVote = chapters is FanficChapterStable.SeparateChaptersModel
        return VoteReaderComponent.State(
            canVote = canVote
        )
    }

    private fun getLastChapterHref(): String {
        val rawHref = if(chapters is FanficChapterStable.SeparateChaptersModel) {
            chapters.chapters.last().href
        } else ""
        return rawHref.substringBefore('#')
    }
    private fun getChapterID(href: String): String {
        return href.substringAfterLast('/')
    }

    private suspend fun dispose() {
        if(autoVoteSetting && lastChapter()) {
            if (state.value.canVote) {
                setVote(true)
            }
        }
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.launch {
                dispose()
                coroutineScope.cancel()
            }
        }
    }
}