package ru.blays.ficbook.reader.shared.components.fanficPageComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.russhwolf.settings.boolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.UrlProcessor.getUrlForHref
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageActionsComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageInfoComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.InternalFanficPageActionsComponent
import ru.blays.ficbook.reader.shared.data.dto.FanficChapterStable
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficPageRepo
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFiltersRepo
import ru.blays.ficbook.reader.shared.platformUtils.copyToClipboard
import ru.blays.ficbook.reader.shared.platformUtils.openInBrowser
import ru.blays.ficbook.reader.shared.platformUtils.runOnUiThread
import ru.blays.ficbook.reader.shared.platformUtils.shareText
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.settings
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue

class DefaultFanficPageInfoComponent(
    componentContext: ComponentContext,
    override val fanficHref: String,
    private val onOutput: (FanficPageInfoComponent.Output) -> Unit
): FanficPageInfoComponent, ComponentContext by componentContext, KoinComponent {
    private val pageRepo: IFanficPageRepo by inject()
    private val filtersRepo: IFiltersRepo by inject()

    private var reverseChaptersOrder by settings.boolean(
        key = SettingsKeys.REVERSE_CHAPTERS_ORDER,
        defaultValue = false
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _state = SaveableMutableValue(
        serializer = FanficPageInfoComponent.State.serializer(),
        initialValue = FanficPageInfoComponent.State(
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
            FanficPageInfoComponent.Intent.Ban -> {
                coroutineScope.launch {
                    val state = state.value
                    val fanfic = state.fanfic
                    if(fanfic != null) {
                        filtersRepo.addFanficToBlacklist(
                            fanficID = fanfic.fanficID,
                            fanficName = fanfic.name
                        )
                        runOnUiThread {
                            onOutput(FanficPageInfoComponent.Output.ClosePage)
                        }
                    }
                }
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
                when (
                    val chapters = state.value.fanfic?.chapters
                ) {
                    is FanficChapterStable.SeparateChaptersModel -> {
                        val commentsHref = "$fanficHref/comments"
                        onOutput(
                            output = FanficPageInfoComponent.Output.OpenAllComments(commentsHref)
                        )
                    }
                    is FanficChapterStable.SingleChapterModel -> {
                        val chapterID = chapters.chapterID
                        onOutput(
                            output = FanficPageInfoComponent.Output.OpenPartComments(chapterID)
                        )
                    }
                    null -> Unit
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
                val pageResult = pageRepo.get(fanficHref)
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
        lifecycle.doOnStart(true) {
            val state = state.value
            if(state.fanfic == null && !state.isError) {
                loadPage()
            }
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}