package ru.blays.ficbook.reader.shared.components.RootComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getFloatFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.RANDOM_FANFIC
import ru.blays.ficbook.api.UrlProcessor.UrlProcessor
import ru.blays.ficbook.api.UrlProcessor.UrlProcessor.analyzeUrl
import ru.blays.ficbook.api.UrlProcessor.getUrlForHref
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.authorProfileComponents.declaration.AuthorProfileComponent
import ru.blays.ficbook.reader.shared.components.authorProfileComponents.implementation.DefaultAuthorProfileComponent
import ru.blays.ficbook.reader.shared.components.collectionComponents.implementation.DefaultCollectionPageComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.implementation.DefaultFanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.implementation.DefaultFanficPageComponent
import ru.blays.ficbook.reader.shared.components.landingScreenComponent.DefaultLandingScreenComponent
import ru.blays.ficbook.reader.shared.components.landingScreenComponent.LandingScreenComponent
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration.MainScreenComponent
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.implemenatation.DefaultMainScreenComponent
import ru.blays.ficbook.reader.shared.components.notificationComponents.DefaultNotificationComponent
import ru.blays.ficbook.reader.shared.components.notificationComponents.NotificationComponent
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileRootComponent
import ru.blays.ficbook.reader.shared.components.profileComponents.implementation.DefaultUserProfileRootComponent
import ru.blays.ficbook.reader.shared.components.searchComponents.implementation.DefaultSearchComponent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsRootComponent
import ru.blays.ficbook.reader.shared.components.settingsComponents.implementation.DefaultSettingsRootComponent
import ru.blays.ficbook.reader.shared.components.themeComponents.DefaultThemeComponent
import ru.blays.ficbook.reader.shared.components.themeComponents.ThemeComponent
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbook.reader.shared.components.usersComponent.implementation.DefaultUsersRootComponent
import ru.blays.ficbook.reader.shared.data.mappers.toApiModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbook.reader.shared.platformUtils.openInBrowser
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.json.GlassEffectConfig
import ru.blays.ficbook.reader.shared.preferences.settings

@OptIn(ExperimentalSettingsApi::class)
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
        initialConfiguration: UserProfileRootComponent.Config,
        output: (UserProfileRootComponent.Output) -> Unit
    ) -> UserProfileRootComponent
): RootComponent, ComponentContext by componentContext, KoinComponent {
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
        userProfile = { componentContext, initialConfiguration, output ->
            DefaultUserProfileRootComponent(
                componentContext = componentContext,
                initialConfiguration = initialConfiguration,
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

    private val authRepo: IAuthorizationRepo by inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val navigation = StackNavigation<RootComponent.Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        initialStack = {
            createInitialConfiguration(deepLink)
        },
        serializer = RootComponent.Config.serializer(),
        childFactory = ::childFactory,
        handleBackButton = true
    )

    override val themeComponent: ThemeComponent = DefaultThemeComponent(
        childContext("theme_component")
    )

    override val glassEffectConfig: StateFlow<GlassEffectConfig> = getGlassEffectConfigFlow()

    override fun sendIntent(intent: RootComponent.Intent) {
        when(intent) {
            is RootComponent.Intent.NewDeepLink -> {
                navigateToLink(intent.deepLink)
            }
        }
    }

    override fun navigateBack() {
        navigation.pop()
    }

    private fun childFactory(
        configuration: RootComponent.Config,
        componentContext: ComponentContext
    ): RootComponent.Child {
        return when(configuration) {
            is RootComponent.Config.UserProfile -> RootComponent.Child.UserProfile(
                userProfile(componentContext, configuration.initialConfiguration, ::onUserProfileOutput)
            )
            is RootComponent.Config.Main -> RootComponent.Child.Main(
                main(componentContext, ::onMainOutput)
            )
            is RootComponent.Config.Settings -> RootComponent.Child.Settings(
                DefaultSettingsRootComponent(
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
                    DefaultCollectionPageComponent(
                        componentContext = componentContext,
                        relativeID = configuration.relativeID,
                        realID = configuration.realID,
                        initialDialogConfig = configuration.initialDialogConfig,
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
            is RootComponent.Config.Landing -> {
                RootComponent.Child.Landing(
                    DefaultLandingScreenComponent(
                        componentContext = componentContext,
                        onOutput = ::onLandingOutput
                    )
                )
            }
            is RootComponent.Config.About -> RootComponent.Child.About(navigation::pop)
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
            is FanficPageComponent.Output.OpenCollection -> {
                navigation.push(
                    configuration = RootComponent.Config.Collection(
                        relativeID = output.relativeID,
                        realID = output.realID,
                        initialDialogConfig = null
                    )
                )
            }
        }
    }

    private fun onMainOutput(output: MainScreenComponent.Output) {
        when(output) {
            is MainScreenComponent.Output.UserProfile -> navigation.push(
                RootComponent.Config.UserProfile(UserProfileRootComponent.Config.Profile)
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
            is MainScreenComponent.Output.OpenAbout -> {
                navigation.push(
                    RootComponent.Config.About
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
                    RootComponent.Config.Collection(
                        relativeID = output.relativeID,
                        realID = output.realID,
                        initialDialogConfig = output.initialDialogConfig
                    )
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
            is UserProfileRootComponent.Output.OpenMainScreen -> {
                navigation.navigate {
                    listOf(RootComponent.Config.Main)
                }
            }
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

    private fun onSettingsOutput(output: SettingsRootComponent.Output) {
        when(output) {
            SettingsRootComponent.Output.NavigateBack -> navigation.pop()
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
            is AuthorProfileComponent.Output.OpenCollection -> {
                navigation.push(
                    RootComponent.Config.Collection(
                        relativeID = output.relativeID,
                        realID = output.realID,
                        initialDialogConfig = null
                    )
                )
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

    private fun onLandingOutput(output: LandingScreenComponent.Output) {
        when(output) {
            LandingScreenComponent.Output.Close -> {
                navigation.navigate { listOf(RootComponent.Config.Main) }
            }
            LandingScreenComponent.Output.OpenLogInScreen -> {
                navigation.push(
                    RootComponent.Config.UserProfile(UserProfileRootComponent.Config.LogIn)
                )
            }
        }
    }

    private fun createInitialConfiguration(link: String?): List<RootComponent.Config> {
        val firstStart = checkFirstStart()
        if(firstStart) {
            return listOf(RootComponent.Config.Landing)
        }
        if(!authRepo.anonymousMode && !authRepo.hasSavedAccount) {
            return listOf(
                RootComponent.Config.Main,
                RootComponent.Config.UserProfile(UserProfileRootComponent.Config.Profile)
            )
        }
        return when(
            val analyzeResult = link?.let { analyzeUrl(it) }
        ) {
            is UrlProcessor.FicbookUrlAnalyzeResult.FanficsList -> {
                listOf(
                    RootComponent.Config.Main,
                    RootComponent.Config.FanficsList(analyzeResult.sectionWithQuery)
                )
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.Fanfic -> {
                listOf(
                    RootComponent.Config.Main,
                    RootComponent.Config.FanficPage(analyzeResult.href)
                )
            }
            is UrlProcessor.FicbookUrlAnalyzeResult.User -> {
                listOf(
                    RootComponent.Config.Main,
                    RootComponent.Config.AuthorProfile(analyzeResult.href)
                )
            }
            else -> {
                listOf(RootComponent.Config.Main)
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

    private fun checkFirstStart(): Boolean {
        return settings.getBoolean(
            key = SettingsKeys.FIRST_START_KEY,
            defaultValue = true
        )
    }

    private fun getGlassEffectConfigFlow(): StateFlow<GlassEffectConfig> {
        val defaultValue = GlassEffectConfig.DEFAULT
        val enabledFlow = settings.getBooleanFlow(
            SettingsKeys.GLASS_EFFECT_ENABLED_KEY,
            defaultValue.enabled
        )
        val alphaFlow = settings.getFloatFlow(
            SettingsKeys.GLASS_EFFECT_ALPHA_KEY,
            defaultValue.alpha
        )
        val radiusFlow = settings.getFloatFlow(
            SettingsKeys.GLASS_EFFECT_RADIUS_KEY,
            defaultValue.blurRadius
        )
        val noiseFactor = settings.getFloatFlow(
            SettingsKeys.GLASS_EFFECT_NOISE_FACTOR_KEY,
            defaultValue.noiseFactor
        )
        return combine(
            enabledFlow,
            alphaFlow,
            radiusFlow,
            noiseFactor
        ) { enabled, alpha, radius, noise ->
            GlassEffectConfig(
                enabled = enabled,
                alpha = alpha,
                blurRadius = radius,
                noiseFactor = noise
            )
        }.stateIn(
            scope = coroutineScope,
            initialValue = defaultValue,
            started = SharingStarted.WhileSubscribed(100)
        )
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}