package ru.blays.ficbookReader.shared.ui.RootComponent

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.*
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.inject
import ru.blays.ficbookReader.shared.data.realmModels.CookieEntity
import ru.blays.ficbookReader.shared.data.realmModels.toApiModel
import ru.blays.ficbookReader.shared.data.realmModels.toEntity
import ru.blays.ficbookReader.shared.ui.fanficListComponents.DefaultFanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.implementation.DefaultFanficPageComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.MainScreenComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.UserLogInComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation.DefaultMainScreenComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation.DefaultUserLogInComponent
import ru.blays.ficbookReader.shared.ui.themeComponents.DefaultThemeComponent
import ru.blays.ficbookReader.shared.ui.themeComponents.ThemeComponent
import ru.blays.ficbookapi.data.SectionWithQuery
import ru.blays.ficbookapi.dataModels.CookieModel
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultRootComponent private constructor(
    private val componentContext: ComponentContext,
    private val fanficPage: (
        componentContext: ComponentContext,
        ficbookApi: IFicbookApi,
        fanficHref: String,
        output: (FanficPageComponent.Output) -> Unit
    ) -> FanficPageComponent,
    private val fanficsList: (
        componentContext: ComponentContext,
        ficbookApi: IFicbookApi,
        section: SectionWithQuery,
        output: (FanficsListComponent.Output) -> Unit
    ) -> FanficsListComponent,
    private val main: (
        componentContext: ComponentContext,
        ficbookApi: IFicbookApi,
        output: (MainScreenComponent.Output) -> Unit
    ) -> MainScreenComponent,
    private val logIn: (
        componentContext: ComponentContext,
        ficbookApi: IFicbookApi,
        output: (UserLogInComponent.Output) -> Unit
    ) -> UserLogInComponent
): RootComponent, ComponentContext by componentContext {
    constructor(
        componentContext: ComponentContext,
    ): this(
        componentContext = componentContext,
        fanficPage = { componentContext, ficbookApi, fanficHref, output ->
            DefaultFanficPageComponent(
                componentContext = componentContext,
                ficbookApi = ficbookApi,
                fanficHref = fanficHref,
                onOutput = output
            )
        },
        fanficsList = { componentContext, ficbookApi, section, output ->
            DefaultFanficsListComponent(
                componentContext = componentContext,
                section = section,
                ficbookApi = ficbookApi,
                output = output
            )
        },
        main = { componentContext, ficbookApi, output ->
            DefaultMainScreenComponent(
                componentContext = componentContext,
                ficbookApi = ficbookApi,
                output = output
            )
        },
        logIn = { componentContext, ficbookApi, output ->
            DefaultUserLogInComponent(
                componentContext = componentContext,
                ficbookApi = ficbookApi,
                output = output
            )
        }
    )

    private val ficbookApi: IFicbookApi = runBlocking { initFicbookApi() }

    private val navigation = StackNavigation<RootComponent.Config>()
    override val childStack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        initialConfiguration = RootComponent.Config.Main,
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

    private fun childFactory(configuration: RootComponent.Config, componentContext: ComponentContext): RootComponent.Child {
        return when(configuration) {
            is RootComponent.Config.Login -> RootComponent.Child.Login(
                logIn(componentContext, ficbookApi, ::onLogInOutput)
            )
            is RootComponent.Config.Main -> RootComponent.Child.Main(
                main(componentContext, ficbookApi, ::onMainOutput)
            )
            is RootComponent.Config.Settings -> TODO()
            is RootComponent.Config.FanficPage -> RootComponent.Child.FanficPage(
                fanficPage(componentContext, ficbookApi, configuration.href, ::onFanficPageOutput)
            )
            is RootComponent.Config.FanficsList -> RootComponent.Child.FanficsList(
                fanficsList(componentContext, ficbookApi, configuration.section, ::onFanficsListOutput)
            )
        }
    }

    private suspend fun initFicbookApi(): IFicbookApi = coroutineScope {
        val ficbookApi: IFicbookApi by inject(IFicbookApi::class.java)
        val cookies = readCookiesFromDB()
        ficbookApi.setCookie(cookies)
        return@coroutineScope ficbookApi
    }

    private fun onFanficPageOutput(output: FanficPageComponent.Output) {
        when(output) {
            FanficPageComponent.Output.NavigateBack -> navigation.pop()
        }
    }

    private fun onMainOutput(output: MainScreenComponent.Output) {
        when(output) {
            is MainScreenComponent.Output.UserButtonClicked -> {
                navigation.push(
                    configuration = RootComponent.Config.Login
                )
            }
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
                    RootComponent.Config.FanficPage("randomfic")
                )
            }
        }
    }

    private fun onLogInOutput(output: UserLogInComponent.Output) {
        when(output) {
            is UserLogInComponent.Output.NavigateBack -> navigation.pop()
            is UserLogInComponent.Output.LogInSuccess -> {
                val cookies = output.cookies
                if(cookies.isNotEmpty()) {
                    coroutineScope.launch {
                        ficbookApi.setCookie(cookies = cookies)
                        writeNewCookiesToDB(cookies = cookies)
                    }
                }
                navigation.pop()
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
        }
    }

    private suspend fun writeNewCookiesToDB(cookies: List<CookieModel>) = coroutineScope {
        val realm: Realm by inject(Realm::class.java) {
            parametersOf(setOf(CookieEntity::class))
        }
        realm.write {
            val savedCookies = query<CookieEntity>().find()
            delete(savedCookies)
        }
        realm.write {
            val newCookies = cookies.map { it.toEntity() }
            newCookies.forEach { cookieEntity ->
                copyToRealm(cookieEntity)
            }
        }
        realm.close()
    }

    private suspend fun readCookiesFromDB(): List<CookieModel> = coroutineScope {
        val realm: Realm by inject(Realm::class.java) {
            parametersOf(setOf(CookieEntity::class))
        }
        val savedCookies = realm.query(CookieEntity::class).find().map {
            it.toApiModel()
        }
        realm.close()
        return@coroutineScope savedCookies
    }
}