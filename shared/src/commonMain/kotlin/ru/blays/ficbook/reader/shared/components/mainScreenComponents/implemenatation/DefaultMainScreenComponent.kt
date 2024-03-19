package ru.blays.ficbook.reader.shared.components.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.data.CollectionsTypes
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponent
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponentInternal
import ru.blays.ficbook.reader.shared.components.collectionComponents.implementation.DefaultCollectionsListComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration.*
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserLogInComponent
import ru.blays.ficbook.reader.shared.components.profileComponents.implementation.DefaultUserLogInComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo

class DefaultMainScreenComponent private constructor(
    componentContext: ComponentContext,
    private val feed: (
        componentContext: ComponentContext,
        output: (FanficsListComponent.Output) -> Unit
    ) -> FeedComponentInternal,
    private val popular: (
        componentContext: ComponentContext,
        output: (PopularSectionsComponent.Output) -> Unit
    ) -> PopularSectionsComponent,
    private val collections: (
        componentContext: ComponentContext,
        sections: Array<SectionWithQuery>,
        output: (CollectionsListComponent.Output) -> Unit
    ) -> CollectionsListComponentInternal,
    private val saved: (
        componentContext: ComponentContext,
        output: (SavedFanficsComponent.Output) -> Unit
    ) -> SavedFanficsComponent,
    private val logIn: (
        componentContext: ComponentContext,
    ) -> UserLogInComponent,
    private val onMainOutput: (MainScreenComponent.Output) -> Unit
): MainScreenComponent, ComponentContext by componentContext {
    constructor(
        componentContext: ComponentContext,
        output: (MainScreenComponent.Output) -> Unit
    ): this(
        componentContext = componentContext,
        feed = { componentContext, output ->
            DefaultFeedComponent(
                componentContext = componentContext,
                onOutput = output
            )
        },
        popular = { componentContext, output ->
            DefaultPopularSectionsComponent(
                componentContext = componentContext,
                onOutput = output
            )
        },
        collections = { componentContext, sections, output ->
            DefaultCollectionsListComponent(
                componentContext = componentContext,
                sections = sections,
                onOutput = output
            )
        },
        saved = { componentContext, output ->
            DefaultSavedFanficsComponent(
                componentContext = componentContext,
                onOutput = output
            )
        },
        logIn = { componentContext ->
            DefaultUserLogInComponent(
                componentContext = componentContext,
                output = {}
            )
        },
        onMainOutput = output
    )

    private val authorizationRepository: IAuthorizationRepo by getKoin().inject()


    override val tabs: Array<MainScreenComponent.TabModel> = arrayOf(
        MainScreenComponent.TabModel(
            0,
            "Лента"
        ),
        MainScreenComponent.TabModel(
            1,
            "Популярное"
        ),
        MainScreenComponent.TabModel(
            2,
            "Сборники"
        )
    )

    override val state = authorizationRepository.currentUserModel

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onOutput(output: MainScreenComponent.Output) = onMainOutput(output)

    private fun onFeedOutput(output: FanficsListComponent.Output) {
        when(output) {
            is FanficsListComponent.Output.OpenFanfic -> {
                onMainOutput(
                    MainScreenComponent.Output.OpenFanficPage(output.href)
                )
            }
            is FanficsListComponent.Output.NavigateBack -> {}
            is FanficsListComponent.Output.OpenAnotherSection -> {
                onMainOutput(
                    MainScreenComponent.Output.OpenFanficsList(output.section)
                )
            }
            is FanficsListComponent.Output.OpenUrl -> onMainOutput(
                MainScreenComponent.Output.OpenUrl(output.url)
            )
            is FanficsListComponent.Output.OpenAuthor -> {
                onMainOutput(
                    MainScreenComponent.Output.OpenAuthor(output.href)
                )
            }
        }
    }

    private fun onPopularOutput(output: PopularSectionsComponent.Output) {
        when(output) {
            is PopularSectionsComponent.Output.NavigateToSection -> {
                this.onMainOutput(
                    MainScreenComponent.Output.OpenFanficsList(
                        output.section
                    )
                )
            }
        }
    }

    private fun onCollectionsOutput(output: CollectionsListComponent.Output) {
        when(output) {
            is CollectionsListComponent.Output.NavigateBack -> Unit
            is CollectionsListComponent.Output.OpenCollection -> {
                onMainOutput(
                    MainScreenComponent.Output.OpenCollection(
                        relativeID = output.relativeID,
                        realID = output.realID,
                        initialDialogConfig = output.initialDialogConfig
                    )
                )
            }
            is CollectionsListComponent.Output.OpenUser -> {
                onMainOutput(
                    MainScreenComponent.Output.OpenAuthor(output.owner.href)
                )
            }
        }
    }

    private fun onSavedOutput(output: SavedFanficsComponent.Output) {
        when(output) {
            is SavedFanficsComponent.Output.NavigateToFanficPage -> TODO()
        }
    }


    private val _feedComponent: FeedComponentInternal = feed(
        childContext(
            key = "feed"
        ),
        ::onFeedOutput
    )
    override val feedComponent: FeedComponent = _feedComponent
    override val popularSectionsComponent: PopularSectionsComponent = popular(
        childContext(
            key = "popular"
        ),
        ::onPopularOutput
    )
    private val _collectionsComponent: CollectionsListComponentInternal = collections(
        childContext(
            key = "collections"
        ),
        getCollectionSections(),
        ::onCollectionsOutput
    )
    override val collectionsComponent: CollectionsListComponent = _collectionsComponent
    override val savedFanficsComponent: SavedFanficsComponent = saved(
        childContext(
            key = "saved"
        ),
        ::onSavedOutput
    )
    override val logInComponent: UserLogInComponent = logIn(
        childContext(
            key = "userLogIn"
        )
    )

    private fun getCollectionSections(): Array<SectionWithQuery> {
        return arrayOf(
            CollectionsTypes.personalCollections,
            CollectionsTypes.trackedCollections
        )
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
        coroutineScope.launch {
            authorizationRepository.currentUserModel.collect { user ->
                if(user != null) {
                    _feedComponent.refresh()
                    _collectionsComponent.refresh()
                }
            }
        }
    }
}