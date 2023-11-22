package ru.blays.ficbookReader.shared.ui.authorProfile.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.pages.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import ru.blays.ficbookReader.shared.data.mappers.toApiModel
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorBlogComponent
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorPresentsComponent
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorProfileComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.CommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.DefaultAllCommentsComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.DefaultFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.AuthorProfileModel
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi
import ru.blays.ficbookapi.result.ApiResult

@OptIn(ExperimentalDecomposeApi::class)
class DefaultAuthorProfileComponent private constructor(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    private val href: String,
    private val blogFactory: (
        childContext: ComponentContext,
        href: String,
        output: (output: AuthorBlogComponent.Output) -> Unit
    ) -> AuthorBlogComponent,
    private val presentsFactory: (
        childContext: ComponentContext,
        href: String,
        output: (output: AuthorPresentsComponent.Output) -> Unit
    ) -> AuthorPresentsComponent,
    private val commentsFactory: (
        childContext: ComponentContext,
        href: String,
        output: (output: CommentsComponent.Output) -> Unit
    ) -> CommentsComponent,
    private val fanficsListFactory: (
        childContext: ComponentContext,
        section: SectionWithQuery,
        output: (output: FanficsListComponent.Output) -> Unit
    ) -> FanficsListComponent,
    private val output: (output: AuthorProfileComponent.Output) -> Unit
): AuthorProfileComponent, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext,
        ficbookApi: IFicbookApi,
        href: String,
        output: (output: AuthorProfileComponent.Output) -> Unit
    ): this(
        componentContext = componentContext,
        ficbookApi = ficbookApi,
        href = href,
        blogFactory = { childContext, href, output ->
              DefaultAuthorBlogComponent(
                  componentContext = childContext,
                  ficbookApi = ficbookApi,
                  href = href,
                  output = output
              )
        },
        presentsFactory = { childContext, href, output ->
            DefaultAuthorPresentsComponent(
                componentContext = childContext,
                ficbookApi = ficbookApi,
                href = href,
                output = output
            )
        },
        commentsFactory = { childContext, href, output ->
            DefaultAllCommentsComponent(
                componentContext = childContext,
                ficbookApi = ficbookApi,
                href = href,
                output = output
            )
        },
        fanficsListFactory = { childContext, section, output ->
            DefaultFanficsListComponent(
                componentContext = childContext,
                section = section,
                ficbookApi = ficbookApi,
                output = output

            )
        },
        output = output
    )

    private val _state: MutableValue<AuthorProfileComponent.State> = MutableValue(
        AuthorProfileComponent.State(
            loading = true,
            error = false,
            errorMessage = null,
            profile = null,
            availableTabs = listOf(AuthorProfileComponent.TabConfig.Main)
        )
    )
    override val state: Value<AuthorProfileComponent.State>
        get() = _state

    private val pagesNavigation = PagesNavigation<AuthorProfileComponent.TabConfig>()

    override val tabs = childPages(
        source = pagesNavigation,
        initialPages = {
            Pages(
                items = state.value.availableTabs,
                selectedIndex = 0
            )
        },
        serializer = AuthorProfileComponent.TabConfig.serializer(),
        handleBackButton = false,
        childFactory = ::tabsChildFactory
    )

    private var blogComponent: AuthorBlogComponent? = null
    private var presentsComponent: AuthorPresentsComponent? = null
    private var commentsComponent: CommentsComponent? = null
    private var worksComponent: FanficsListComponent? = null
    private var worksAsCoauthorComponent: FanficsListComponent? = null
    private var worksAsBetaComponent: FanficsListComponent? = null
    private var worksAsGammaComponent: FanficsListComponent? = null

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: AuthorProfileComponent.Intent) {
        when(intent) {
            is AuthorProfileComponent.Intent.Follow -> {
                // TODO
            }
            AuthorProfileComponent.Intent.Refresh -> {
                loadProfile()
            }
            is AuthorProfileComponent.Intent.SelectTabs -> {
                if(intent.index != tabs.value.selectedIndex) {
                    pagesNavigation.select(intent.index)
                }
            }
        }
    }

    override fun onOutput(output: AuthorProfileComponent.Output) {
        this.output(output)
    }

    private fun blogOutput(output: AuthorBlogComponent.Output) {
        when(output) {
            is AuthorBlogComponent.Output.OpenUrl -> {
                this.output(
                    AuthorProfileComponent.Output.OpenUrl(
                        url = output.url
                    )
                )
            }
        }
    }

    private fun presentsOutput(output: AuthorPresentsComponent.Output) {
        when(output) {
            is AuthorPresentsComponent.Output.OpenAnotherProfile -> {
                this.output(
                    AuthorProfileComponent.Output.OpenAnotherProfile(
                        href = output.href
                    )
                )
            }
            is AuthorPresentsComponent.Output.OpenFanfic -> {
                this.output(
                    AuthorProfileComponent.Output.OpenFanfic(
                        href = output.href
                    )
                )
            }
        }
    }

    private fun commentsOutput(output: CommentsComponent.Output) {
        when(output) {
            CommentsComponent.Output.NavigateBack -> {}
            is CommentsComponent.Output.OpenAuthor -> {
                this.output(
                    AuthorProfileComponent.Output.OpenAnotherProfile(
                        href = output.href
                    )
                )
            }
            is CommentsComponent.Output.OpenUrl -> {
                this.output(
                    AuthorProfileComponent.Output.OpenUrl(
                        url = output.url
                    )
                )
            }
            is CommentsComponent.Output.OpenFanfic -> {
                this.output(
                    AuthorProfileComponent.Output.OpenFanfic(
                        href = output.href
                    )
                )
            }
        }
    }

    private fun fanficsListOutput(output: FanficsListComponent.Output) {
        when(output) {
            is FanficsListComponent.Output.NavigateBack -> {}
            is FanficsListComponent.Output.OpenAnotherSection -> {
                this.output(
                    AuthorProfileComponent.Output.OpenFanficsList(
                        section = output.section.toApiModel()
                    )
                )
            }
            is FanficsListComponent.Output.OpenFanfic -> {
                this.output(
                    AuthorProfileComponent.Output.OpenFanfic(
                        href = output.href
                    )
                )
            }
            is FanficsListComponent.Output.OpenUrl -> {
                this.output(
                    AuthorProfileComponent.Output.OpenUrl(
                        url = output.url
                    )
                )
            }
            is FanficsListComponent.Output.OpenAuthor -> {
                this.output(
                    AuthorProfileComponent.Output.OpenAnotherProfile(
                        href = output.href
                    )
                )
            }
        }
    }

    private fun tabsChildFactory(
        tabConfig: AuthorProfileComponent.TabConfig,
        componentContext: ComponentContext
    ): AuthorProfileComponent.Tabs {
        return when(tabConfig) {
            is AuthorProfileComponent.TabConfig.Main -> {
                AuthorProfileComponent.Tabs.Main(this)
            }
            is AuthorProfileComponent.TabConfig.Blog -> AuthorProfileComponent.Tabs.Blog(
                component = blogComponent!!
            )
            is AuthorProfileComponent.TabConfig.Presents -> {
                AuthorProfileComponent.Tabs.Presents(
                    component = presentsComponent!!
                )
            }
            is AuthorProfileComponent.TabConfig.Works -> {
                AuthorProfileComponent.Tabs.Works(
                    component = worksComponent!!
                )
            }
            is AuthorProfileComponent.TabConfig.WorksAsBeta -> {
                AuthorProfileComponent.Tabs.WorksAsBeta(
                    component = worksAsBetaComponent!!
                )
            }
            is AuthorProfileComponent.TabConfig.WorksAsCoauthor -> {
                AuthorProfileComponent.Tabs.WorksAsCoauthor(
                    component = worksAsCoauthorComponent!!
                )
            }
            is AuthorProfileComponent.TabConfig.WorksAsGamma -> {
                AuthorProfileComponent.Tabs.WorksAsGamma(
                    component = worksAsGammaComponent!!
                )
            }
            is AuthorProfileComponent.TabConfig.Comments -> {
                AuthorProfileComponent.Tabs.Comments(
                    component = commentsComponent!!
                )
            }
        }
    }

    private fun loadProfile() = coroutineScope.launch {
        _state.update {
            it.copy(
                loading = true
            )
        }
        when(
            val result = ficbookApi.getAuthorProfileForHref(href)
        ) {
            is ApiResult.Error -> {
                _state.update {
                    clearComponents()
                    it.copy(
                        loading = false,
                        error = true,
                        errorMessage = result.message
                    )
                }
            }
            is ApiResult.Success -> {
                val availableTabs = createComponentsForProfile(result.value)
                transformTabsStack(availableTabs)
                _state.update {
                    it.copy(
                        loading = false,
                        error = false,
                        profile = result.value.toStableModel(),
                        availableTabs = availableTabs
                    )
                }
            }
        }
    }

    private suspend fun createComponentsForProfile(profile: AuthorProfileModel): List<AuthorProfileComponent.TabConfig> {
        val availableTabs: MutableList<AuthorProfileComponent.TabConfig> = mutableListOf()

        blogComponent = blogFactory(
            childContext("blogComponent"),
            profile.authorBlogHref,
            ::blogOutput
        )
        availableTabs += AuthorProfileComponent.TabConfig.Blog(
            href = profile.authorBlogHref
        )
        /*presentsComponent = presentsFactory(
            childContext("presentsComponent"),
            profile.authorPresentsHref,
            ::presentsOutput
        )
        availableTabs += AuthorProfileComponent.TabConfig.Presents(
            href = profile.authorPresentsHref
        )*/
        commentsComponent = commentsFactory(
            childContext("commentsComponent"),
            profile.authorCommentsHref,
            ::commentsOutput
        )
        availableTabs += AuthorProfileComponent.TabConfig.Comments(
            href = profile.authorCommentsHref
        )
        profile.authorWorks?.let { section ->
            worksComponent = fanficsListFactory(
                childContext("worksComponent"),
                section,
                ::fanficsListOutput
            )
            availableTabs += AuthorProfileComponent.TabConfig.Works(
                section = section
            )
        }
        profile.authorWorksAsCoauthor?.let { section ->
            worksAsCoauthorComponent = fanficsListFactory(
                childContext("worksAsCoauthorComponent"),
                section,
                ::fanficsListOutput
            )
            availableTabs += AuthorProfileComponent.TabConfig.WorksAsCoauthor(
                section = section
            )
        }
        profile.authorWorksAsBeta?.let { section ->
            worksAsBetaComponent = fanficsListFactory(
                childContext("worksAsBetaComponent"),
                section,
                ::fanficsListOutput
            )
            availableTabs += AuthorProfileComponent.TabConfig.WorksAsBeta(
                section = section
            )
        }
        profile.authorWorksAsGamma?.let { section ->
            worksAsGammaComponent = fanficsListFactory(
                childContext("worksAsGammaComponent"),
                section,
                ::fanficsListOutput
            )
            availableTabs += AuthorProfileComponent.TabConfig.WorksAsGamma(
                section = section
            )
        }
        return availableTabs
    }

    private fun clearComponents() {
        transformTabsStack(null)
        blogComponent = null
        presentsComponent = null
        worksComponent = null
        worksAsCoauthorComponent = null
        worksAsBetaComponent = null
        worksAsGammaComponent = null
    }

    private fun transformTabsStack(availableTabs: List<AuthorProfileComponent.TabConfig>?) {
        pagesNavigation.navigate {
            if(availableTabs == null) {
                return@navigate Pages(
                    items = listOf(AuthorProfileComponent.TabConfig.Main),
                    selectedIndex = 0
                )
            }
            return@navigate if(it.items.containsAll(availableTabs)) {
                val newItems = it.items.dropLastWhile { config ->
                    config != AuthorProfileComponent.TabConfig.Main
                }
                val newIndex = newItems.indexOf(
                    it.items[it.selectedIndex]
                )
                it.copy(
                    items = newItems,
                    selectedIndex = newIndex
                )
            } else {
                val newItems =
                    listOf(AuthorProfileComponent.TabConfig.Main) + availableTabs
                it.copy(
                    items = newItems,
                    selectedIndex = 0
                )
            }
        }
    }

    init {
        loadProfile()
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
            clearComponents()
        }
    }
}