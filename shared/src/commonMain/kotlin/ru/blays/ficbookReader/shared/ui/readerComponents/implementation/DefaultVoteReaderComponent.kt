package ru.blays.ficbookReader.shared.ui.readerComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbookReader.shared.ui.readerComponents.declaration.VoteReaderComponent

class DefaultVoteReaderComponent(
    componentContext: ComponentContext,
    chapters: List<FanficChapterStable>,
    private val fanficID: String
): VoteReaderComponent, ComponentContext by componentContext {
    private val fanficPageRepo: IFanficPageRepo by getKoin().inject()

    private val separateChapters = chapters.filterIsInstance(
        FanficChapterStable.SeparateChapterModel::class.java
    )

    private val _state = MutableValue(calculateState())
    override val state: Value<VoteReaderComponent.State>
        get() = _state

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val settingsRepository: ISettingsRepository by inject(ISettingsRepository::class.java)
    private val autoVoteSetting by settingsRepository.getDelegate(
        key = ISettingsRepository.booleanKey(SettingsKeys.AUTO_VOTE_FOR_CONTINUE),
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
        val canVote = separateChapters.isNotEmpty()
        return VoteReaderComponent.State(
            canVote = canVote
        )
    }

    private fun getLastChapterHref(): String {
        val rawHref = separateChapters.last().href
        return rawHref.substringBefore('#')
    }
    private fun getChapterID(href: String): String {
        return href.substringAfterLast('/')
    }

    private suspend fun dispose() {
        if(autoVoteSetting) {
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