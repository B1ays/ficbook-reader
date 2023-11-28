package ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.*
import ru.blays.ficbookReader.shared.ui.profileComponents.DefaultUserLogInComponent
import ru.blays.ficbookReader.shared.ui.profileComponents.UserLogInComponent

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
        output: (CollectionsComponent.Output) -> Unit
    ) -> CollectionsComponentInternal,
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
        collections = { componentContext, output ->
            DefaultCollectionsComponent(
                componentContext = componentContext,
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
        ),
        MainScreenComponent.TabModel(
            3,
            "Сохранённые"
        )
    )

    private val _state: MutableValue<MainScreenComponent.State> = MutableValue(
        MainScreenComponent.State(
            user = authorizationRepository.currentUser.value,
            authorized = authorizationRepository.authorized.value
        )
    )

    override val state: Value<MainScreenComponent.State>
        get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onOutput(output: MainScreenComponent.Output) = onMainOutput(output)

    private fun onFeedOutput(output: FanficsListComponent.Output) {
        when(output) {
            is FanficsListComponent.Output.OpenFanfic -> {
                onMainOutput(
                    MainScreenComponent.Output.OpenFanficPage(output.href)
                )
            }

            FanficsListComponent.Output.NavigateBack -> {}
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

    private fun onCollectionsOutput(output: CollectionsComponent.Output) {
        when(output) {
            is CollectionsComponent.Output.OpenCollection -> {
                this.onMainOutput(
                    MainScreenComponent.Output.OpenFanficsList(output.section)
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
    private val _collectionsComponent: CollectionsComponentInternal = collections(
        childContext(
            key = "collections"
        ),
        ::onCollectionsOutput
    )
    override val collectionsComponent: CollectionsComponent = _collectionsComponent

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


    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
        coroutineScope.launch {
            authorizationRepository.currentUser.collect { newUser ->
                _state.update {
                    it.copy(user = newUser)
                }
            }
        }
        coroutineScope.launch {
            authorizationRepository.authorized.collect { authorized ->
                _state.update {
                    it.copy(authorized = authorized)
                }
                if(authorized) {
                    _feedComponent.refresh()
                    _collectionsComponent.refresh()
                }
            }
        }
    }
}