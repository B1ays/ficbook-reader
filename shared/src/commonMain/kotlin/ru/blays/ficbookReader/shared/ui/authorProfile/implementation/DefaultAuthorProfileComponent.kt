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
import org.koin.java.KoinJavaComponent
import ru.blays.ficbookReader.shared.data.mappers.toApiModel
import ru.blays.ficbookReader.shared.data.repo.declaration.IAuthorProfileRepo
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorBlogComponent
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorPresentsComponent
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorProfileComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.CommentsComponent
import ru.blays.ficbookReader.shared.ui.commentsComponent.DefaultAllCommentsComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.DefaultFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.AuthorProfileTabs
import ru.blays.ficbookapi.result.ApiResult

@OptIn(ExperimentalDecomposeApi::class)
class DefaultAuthorProfileComponent private constructor(
    componentContext: ComponentContext,
    private val href: String,
    private val blogFactory: (
        childContext: ComponentContext,
        id: String,
        output: (output: AuthorBlogComponent.Output) -> Unit
    ) -> AuthorBlogComponent,
    private val presentsFactory: (
        childContext: ComponentContext,
        href: String,
        output: (output: AuthorPresentsComponent.Output) -> Unit
    ) -> AuthorPresentsComponent,
    private val commentsFactory: (
        childContext: ComponentContext,
        userID: String,
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
        href: String,
        output: (output: AuthorProfileComponent.Output) -> Unit
    ): this(
        componentContext = componentContext,
        href = href,
        blogFactory = { childContext, userID, output ->
              DefaultAuthorBlogComponent(
                  componentContext = childContext,
                  userID = userID,
                  output = output
              )
        },
        presentsFactory = { childContext, href, output ->
            DefaultAuthorPresentsComponent(
                componentContext = childContext,
                href = href,
                output = output
            )
        },
        commentsFactory = { childContext, userID, output ->
            DefaultAllCommentsComponent(
                componentContext = childContext,
                href = "authors/$userID/comments",
                output = output
            )
        },
        fanficsListFactory = { childContext, section, output ->
            DefaultFanficsListComponent(
                componentContext = childContext,
                section = section,
                output = output
            )
        },
        output = output
    )

    private val authorProfileRepo: IAuthorProfileRepo by KoinJavaComponent.getKoin().inject()

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
            val result = authorProfileRepo.getByHref(href)
        ) {
            is ApiResult.Error -> {
                _state.update {
                    clearComponents()
                    it.copy(
                        loading = false,
                        error = true,
                        errorMessage = result.exception.message
                    )
                }
            }
            is ApiResult.Success -> {
                val availableTabs = createComponentsForProfile(
                    userID = result.value.authorMain.id,
                    tabs = result.value.availableTabs
                )
                transformTabsStack(availableTabs)
                _state.update {
                    it.copy(
                        loading = false,
                        error = false,
                        profile = result.value,
                        availableTabs = availableTabs
                    )
                }
            }
        }
    }

    private suspend fun createComponentsForProfile(
        userID: String,
        tabs: List<AuthorProfileTabs>
    ): List<AuthorProfileComponent.TabConfig> {
        val availableTabs: MutableList<AuthorProfileComponent.TabConfig> = mutableListOf()

        tabs.forEach { 
            when(it) {
                AuthorProfileTabs.BLOG -> {
                    blogComponent = blogFactory(
                        childContext("blogComponent"),
                        userID,
                        ::blogOutput
                    )
                    availableTabs += AuthorProfileComponent.TabConfig.Blog(
                        userID = userID
                    )
                }
                AuthorProfileTabs.WORKS -> {
                    val section = SectionWithQuery(
                        href = "authors/$userID/profile/works"
                    )
                    worksComponent = fanficsListFactory(
                        childContext("worksComponent"),
                        section,
                        ::fanficsListOutput
                    )
                    availableTabs += AuthorProfileComponent.TabConfig.Works(
                        section = section
                    )
                }
                AuthorProfileTabs.WORKS_COAUTHOR -> {
                    val section = SectionWithQuery(
                        href = "authors/$userID/profile/coauthor"
                    )
                    worksAsCoauthorComponent = fanficsListFactory(
                        childContext("worksAsCoauthorComponent"),
                        section,
                        ::fanficsListOutput
                    )
                    availableTabs += AuthorProfileComponent.TabConfig.WorksAsCoauthor(
                        section = section
                    )
                }
                AuthorProfileTabs.WORKS_BETA -> {
                    val section = SectionWithQuery(
                        href = "authors/$userID/profile/beta"
                    )
                    worksAsBetaComponent = fanficsListFactory(
                        childContext("worksAsBetaComponent"),
                        section,
                        ::fanficsListOutput
                    )
                    availableTabs += AuthorProfileComponent.TabConfig.WorksAsBeta(
                        section = section
                    )
                }
                AuthorProfileTabs.WORKS_GAMMA -> {
                    val section = SectionWithQuery(
                        href = "authors/$userID/profile/gamma"
                    )
                    worksAsGammaComponent = fanficsListFactory(
                        childContext("worksAsGammaComponent"),
                        section,
                        ::fanficsListOutput
                    )
                    availableTabs += AuthorProfileComponent.TabConfig.WorksAsGamma(
                        section = section
                    )
                }
                AuthorProfileTabs.REQUESTS -> {

                }
                AuthorProfileTabs.COLLECTIONS -> {

                }
                AuthorProfileTabs.PRESENTS -> {

                }
                AuthorProfileTabs.COMMENTS -> {
                    commentsComponent = commentsFactory(
                        childContext("commentsComponent"),
                        userID,
                        ::commentsOutput
                    )
                    availableTabs += AuthorProfileComponent.TabConfig.Comments(
                        userID = href
                    )
                }
            }
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