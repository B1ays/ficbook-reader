package ru.blays.ficbookReader.shared.ui.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.russhwolf.settings.boolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.dto.FanficChapterStable
import ru.blays.ficbookReader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbookReader.shared.platformUtils.copyToClipboard
import ru.blays.ficbookReader.shared.platformUtils.openInBrowser
import ru.blays.ficbookReader.shared.platformUtils.shareText
import ru.blays.ficbookReader.shared.preferences.SettingsKeys
import ru.blays.ficbookReader.shared.preferences.settings
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageActionsComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.InternalFanficPageActionsComponent
import ru.blays.ficbookapi.UrlProcessor.getUrlForHref
import ru.blays.ficbookapi.result.ApiResult

class DefaultFanficPageInfoComponent(
    componentContext: ComponentContext,
    private val fanficHref: String,
    private val onOutput: (FanficPageInfoComponent.Output) -> Unit
): FanficPageInfoComponent, ComponentContext by componentContext {
    private val repository: IFanficPageRepo by getKoin().inject()

    private var reverseChaptersOrder by settings.boolean(
        key = SettingsKeys.REVERSE_CHAPTERS_ORDER,
        defaultValue = false
    )

    private val _state = MutableValue(
        FanficPageInfoComponent.State(
            reverseOrderEnabled = reverseChaptersOrder
        )
    )
    override val state: Value<FanficPageInfoComponent.State>
        get() = _state

    private val _actionsComponent: InternalFanficPageActionsComponent = DefaultFanficPageActionsComponent(
        componentContext = childContext(
            key = "actions"
        ),
        output = ::onActionsOutput
    )

    override val actionsComponent: FanficPageActionsComponent
        get() = _actionsComponent

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: FanficPageInfoComponent.Intent) {
        when(intent) {
            is FanficPageInfoComponent.Intent.Refresh -> loadPage()
            is FanficPageInfoComponent.Intent.CopyLink -> {
                val link = getUrlForHref(href = fanficHref)
                copyToClipboard(link)
            }
            is FanficPageInfoComponent.Intent.Share -> {
                val link = getUrlForHref(href = fanficHref)
                val textToShare = "${state.value.fanfic?.name} - $link"
                shareText(textToShare)
            }
            FanficPageInfoComponent.Intent.OpenInBrowser -> {
                val link = getUrlForHref(href = fanficHref)
                openInBrowser(link)
            }
            is FanficPageInfoComponent.Intent.ChangeChaptersOrder -> setChaptersOrder(intent.reverse)
        }
    }

    override fun onOutput(output: FanficPageInfoComponent.Output) {
        onOutput.invoke(output)
    }

    private fun onActionsOutput(output: FanficPageActionsComponent.Output) {
        when(output) {
            FanficPageActionsComponent.Output.OpenComments -> {
                if(state.value.fanfic?.chapters is FanficChapterStable.SeparateChaptersModel) {
                    val commentsHref = "$fanficHref/comments"
                    onOutput(
                        FanficPageInfoComponent.Output.OpenAllComments(commentsHref)
                    )
                } else if(state.value.fanfic?.chapters is FanficChapterStable.SingleChapterModel) {
                    val chapterID = state.value.fanfic?.fanficID ?: return
                    onOutput(
                        FanficPageInfoComponent.Output.OpenPartComments(chapterID)
                    )
                }
            }
        }
    }

    private fun setChaptersOrder(reverse: Boolean) {
        reverseChaptersOrder = reverse
        _state.update {
            it.copy(
                reverseOrderEnabled = reverse,
            )
        }
    }

    private fun loadPage() {
        coroutineScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            when(
                val pageResult = repository.get(fanficHref)
            ) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            fanfic = pageResult.value,
                            isLoading = false
                        )
                    }
                    _actionsComponent.setValue(
                        FanficPageActionsComponent.State(
                            follow = pageResult.value.subscribed,
                            mark = pageResult.value.liked
                        )
                    )
                    _actionsComponent.setFanficID(pageResult.value.fanficID)
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(isError = true)
                    }
                }
            }
        }
    }

    init {
        loadPage()
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}