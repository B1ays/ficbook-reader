package ru.blays.ficbookReader.shared.ui.RootComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import ru.blays.ficbookReader.shared.data.mappers.toApiModel
import ru.blays.ficbookReader.shared.platformUtils.openInBrowser
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorProfileComponent
import ru.blays.ficbookReader.shared.ui.authorProfile.implementation.DefaultAuthorProfileComponent
import ru.blays.ficbookReader.shared.ui.collectionSortComponent.DefaultCollectionFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.DefaultFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.implementation.DefaultFanficPageComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.MainScreenComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation.DefaultMainScreenComponent
import ru.blays.ficbookReader.shared.ui.notificationComponents.DefaultNotificationComponent
import ru.blays.ficbookReader.shared.ui.notificationComponents.NotificationComponent
import ru.blays.ficbookReader.shared.ui.profileComponents.DefaultUserProfileRootComponent
import ru.blays.ficbookReader.shared.ui.profileComponents.UserProfileRootComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.implementation.DefaultSearchComponent
import ru.blays.ficbookReader.shared.ui.settingsComponents.declaration.SettingsMainComponent
import ru.blays.ficbookReader.shared.ui.settingsComponents.implementation.DefaultSettingsMainComponent
import ru.blays.ficbookReader.shared.ui.themeComponents.DefaultThemeComponent
import ru.blays.ficbookReader.shared.ui.themeComponents.ThemeComponent
import ru.blays.ficbookReader.shared.ui.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbookReader.shared.ui.usersComponent.implementation.DefaultUsersRootComponent
import ru.blays.ficbookapi.RANDOM_FANFIC
import ru.blays.ficbookapi.UrlProcessor.UrlProcessor
import ru.blays.ficbookapi.UrlProcessor.UrlProcessor.analyzeUrl
import ru.blays.ficbookapi.UrlProcessor.getUrlForHref
import ru.blays.ficbookapi.data.SectionWithQuery

class DefaultRootComponent private constructor(
    private val componentContext: ComponentContext,
    private val deepLink: String? = null,
    private val fanficPage: (
        componentContext: ComponentContext,
        fanficHref: String,
        output: (FanficPageComponent.Output) -> Unit
    ) -> FanficPageComponent,
    private val fanficsList: (
        componentContext: ComponentContext,
        section: SectionWithQuery,
        output: (FanficsListComponent.Output) -> Unit
    ) -> FanficsListComponent,
    private val main: (
        componentContext: ComponentContext,
        output: (MainScreenComponent.Output) -> Unit
    ) -> MainScreenComponent,
    private val userProfile: (
        componentContext: ComponentContext,
        output: (UserProfileRootComponent.Output) -> Unit
    ) -> UserProfileRootComponent
): RootComponent, ComponentContext by componentContext {
    constructor(
        componentContext: ComponentContext,
        deepLink: String?
    ): this(
        componentContext = componentContext,
        deepLink = deepLink,
        fanficPage = { componentContext, fanficHref, output ->
            DefaultFanficPageComponent(
                componentContext = componentContext,
                fanficHref = fanficHref,
                onOutput = output
            )
        },
        fanficsList = { componentContext, section, output ->
            DefaultFanficsListComponent(
                componentContext = componentContext,
                section = section,
                output = output
            )
        },
        main = { componentContext, output ->
            DefaultMainScreenComponent(
                componentContext = componentContext,
                output = output
            )
        },
        userProfile = { componentContext, output ->
            DefaultUserProfileRootComponent(
                componentContext = componentContext,
                output = output
            )
        }
    )

    constructor(
        componentContext: ComponentContext
    ): this(
        componentContext = componentContext,
        deepLink = null
    )

    private val navigation = StackNavigation<RootComponent.Config>()
    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        initialConfiguration = getConfigurationForLink(deepLink),
        serializer = RootComponent.Config.serializer(),
        childFactory = ::childFactory,
        handleBackButton = true
    )

    override val themeComponent: ThemeComponent = DefaultThemeComponent(
        childContext("theme_component")
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

    override fun sendIntent(intent: RootComponent.Intent) {
        when(intent) {
            is RootComponent.Intent.NewDeepLink -> {
                navigateToLink(intent.deepLink)
            }
        }
    }

    private fun childFactory(configuration: RootComponent.Config, componentContext: ComponentContext): RootComponent.Child {
        return when(configuration) {
            is RootComponent.Config.UserProfile -> RootComponent.Child.UserProfile(
                userProfile(componentContext, ::onUserProfileOutput)
            )
            is RootComponent.Config.Main -> RootComponent.Child.Main(
                main(componentContext, ::onMainOutput)
            )
            is RootComponent.Config.Settings -> RootComponent.Child.Settings(
                DefaultSettingsMainComponent(
                    componentContext, ::onSettingsOutput
                )
            )
            is RootComponent.Config.FanficPage -> RootComponent.Child.FanficPage(
                fanficPage(componentContext, configuration.href, ::onFanficPageOutput)
            )
            is RootComponent.Config.FanficsList -> RootComponent.Child.FanficsList(
                fanficsList(componentContext, configuration.section, ::onFanficsListOutput)
            )
            is RootComponent.Config.AuthorProfile -> RootComponent.Child.AuthorProfile(
                DefaultAuthorProfileComponent(
                    componentContext = componentContext,
                    href = configuration.href,
                    output = ::onAuthorProfileOutput
                )
            )
            is RootComponent.Config.Collection -> {
                RootComponent.Child.Collection(
                    DefaultCollectionFanficsListComponent(
                        componentContext = componentContext,
                        initialSection = configuration.section,
                        output = ::onFanficsListOutput
                    )
                )
            }
            is RootComponent.Config.Users -> {
                RootComponent.Child.Users(
                    DefaultUsersRootComponent(
                        componentContext = componentContext,
                        output = ::onUsersOutput
                    )
                )
            }
            is RootComponent.Config.Notifications -> {
                RootComponent.Child.Notifications(
                    DefaultNotificationComponent(
                        componentContext = componentContext,
                        output = ::onNotificationsOutput
                    )
                )
            }
            is RootComponent.Config.Search -> {
                RootComponent.Child.Search(
                    DefaultSearchComponent(
                        componentContext = componentContext,
                        output = ::onFanficsListOutput
                    )
                )
            }
        }
    }

    private fun onFanficPageOutput(output: FanficPageComponent.Output) {
        when(output) {
            FanficPageComponent.Output.NavigateBack -> navigation.pop()
            is FanficPageComponent.Output.OpenUrl -> navigateToLink(output.url)
            is FanficPageComponent.Output.OpenSection -> {
                navigation.push(
                    configuration = RootComponent.Config.FanficsList(
                        section = output.section.toApiModel()
                    )
                )
            }
            is FanficPageComponent.Output.OpenAuthor -> {
                navigation.push(
                    configuration = RootComponent.Config.AuthorProfile(
                        href = output.href
                    )
                )
            }
            is FanficPageComponent.Output.OpenAnotherFanfic -> {
                navigation.push(
                    configuration = RootComponent.Config.FanficPage(output.href)
                )
            }
        }
    }

    private fun onMainOutput(output: MainScreenComponent.Output) {
        when(output) {
            is MainScreenComponent.Output.UserProfile -> navigation.push(
                RootComponent.Config.UserProfile
            )
            is MainScreenComponent.Output.OpenFanficPage -> {
                navigation.push(
                    RootComponent.Config.FanficPage(output.href)
                )
            }
            is MainScreenComponent.Output.OpenFanficsList -> {
                navigation.push(
                    RootComponent.Config.FanficsList(output.sectionWithQuery)
                )
            }
            is MainScreenComponent.Output.OpenRandomFanficPage -> {
                navigation.push(
                    RootComponent.Config.FanficPage(RANDOM_FANFIC)
                )
            }
            is MainScreenComponent.Output.OpenSettings -> {
                navigation.push(
                    RootComponent.Config.Settings
                )
            }
            is MainScreenComponent.Output.OpenUrl -> navigateToLink(output.url)
            is MainScreenComponent.Output.OpenAuthor -> {
                navigation.push(
                    RootComponent.Config.AuthorProfile(
                        href = output.href
                    )
                )
            }
            is MainScreenComponent.Output.OpenCollection -> {
                navigation.push(
                    RootComponent.Config.Collection(output.section)
                )
            }
            is MainScreenComponent.Output.OpenUsersScreen -> {
                navigation.push(
                    RootComponent.Config.Users
                )
            }
            is MainScreenComponent.Output.OpenNotifications -> {
                navigation.push(
                    RootComponent.Config.Notifications
                )
            }
            is MainScreenComponent.Output.Search -> {
                navigation.push(
                    RootComponent.Config.Search
                )
            }
        }
    }

    private fun onUserProfileOutput(output: UserProfileRootComponent.Output) {
        when(output) {
            is UserProfileRootComponent.Output.NavigateBack -> navigation.pop()
            is UserProfileRootComponent.Output.OpenProfile -> navigation.push(
                RootComponent.Config.AuthorProfile(
                    href = output.userHref
                )
            )
        }
    }

    private fun onFanficsListOutput(output: FanficsListComponent.Output) {
        when(output) {
            is FanficsListComponent.Output.OpenFanfic -> {
                navigation.push(
                    RootComponent.Config.FanficPage(output.href)
                )
            }
            is FanficsListComponent.Output.NavigateBack -> navigation.pop()
            is FanficsListComponent.Output.OpenAnotherSection -> {
                navigation.push(
                    RootComponent.Config.FanficsList(
                        section = output.section.toApiModel()
                    )
                )
            }
            is FanficsListComponent.Output.OpenUrl -> navigateToLink(output.url)
            is FanficsListComponent.Output.OpenAuthor -> {
                navigation.push(
                    RootComponent.Config.AuthorProfile(
                        href = output.href
                    )
                )
            }
        }
    }

    private fun onSettingsOutput(output: SettingsMainComponent.Output) {
        when(output) {
            SettingsMainComponent.Output.NavigateBack -> navigation.pop()
        }
    }

    private fun onAuthorProfileOutput(output: AuthorProfileComponent.Output) {
        when(output) {
            is AuthorProfileComponent.Output.NavigateBack -> {
                navigation.pop()
            }
            is AuthorProfileComponent.Output.OpenAnotherProfile -> {
                navigation.push(
                    RootComponent.Config.AuthorProfile(
                        href = output.href
                    )
                )
            }
            is AuthorProfileComponent.Output.OpenFanfic -> {
                navigation.push(
                    RootComponent.Config.FanficPage(
                        href = output.href
                    )
                )
            }
            is AuthorProfileComponent.Output.OpenFanficsList -> {
                navigation.push(
                    RootComponent.Config.FanficsList(
                        section = output.section
                    )
                )
            }
            is AuthorProfileComponent.Output.OpenUrl -> {
                navigateToLink(output.url)
            }
        }
    }

    private fun onUsersOutput(output: UsersRootComponent.Output) {
        when(output) {
            UsersRootComponent.Output.NavigateBack -> navigation.pop()
            is UsersRootComponent.Output.OpenAuthorProfile -> navigation.push(
                RootComponent.Config.AuthorProfile(
                    href = output.href
                )
            )
        }
    }

    private fun onNotificationsOutput(output: NotificationComponent.Output) {
        when(output) {
            is NotificationComponent.Output.NavigateBack -> navigation.pop()
            is NotificationComponent.Output.OpenNotificationHref -> navigateToLink(
                link = getUrlForHref(output.href)
            )
        }
    }

    private fun getConfigurationForLink(link: String?): RootComponent.Config {
        if(link == null) return RootComponent.Config.Main
        return when(
            val analyzeResult = analyzeUrl(link)
        ) {
            is UrlProcessor.FicbookUrlAnalyzeResult.FanficsList -> {
                RootComponent.Config.FanficsList(analyzeResult.sectionWithQuery)
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.Fanfic -> {
                RootComponent.Config.FanficPage(analyzeResult.href)
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.User -> {
                RootComponent.Config.AuthorProfile(analyzeResult.href)
            }
            else -> {
                RootComponent.Config.Main
            }
        }
    }

    private fun navigateToLink(link: String) {
        when(
            val analyzeResult = analyzeUrl(link)
        ) {
            is UrlProcessor.FicbookUrlAnalyzeResult.FanficsList -> {
                navigation.push(
                    RootComponent.Config.FanficsList(analyzeResult.sectionWithQuery)
                )
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.Fanfic -> {
                navigation.push(
                    RootComponent.Config.FanficPage(analyzeResult.href)
                )
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.User -> {
                navigation.push(
                    RootComponent.Config.AuthorProfile(analyzeResult.href)
                )
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.Notifications -> {
                navigation.push(
                    RootComponent.Config.Notifications
                )
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.NotFicbookUrl -> {
                openInBrowser(link)
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.NotAUrl -> {
                println("Error, link: $link is incorrect")
            }
        }
    }
}