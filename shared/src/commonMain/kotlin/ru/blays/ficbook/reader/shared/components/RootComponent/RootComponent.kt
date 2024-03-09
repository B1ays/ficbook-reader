package ru.blays.ficbook.reader.shared.components.RootComponent

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.authorProfile.declaration.AuthorProfileComponent
import ru.blays.ficbook.reader.shared.components.collectionSortComponent.CollectionFanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.components.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbook.reader.shared.components.landingScreenComponent.LandingScreenComponent
import ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration.MainScreenComponent
import ru.blays.ficbook.reader.shared.components.notificationComponents.NotificationComponent
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileRootComponent
import ru.blays.ficbook.reader.shared.components.searchComponents.declaration.SearchComponent
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsMainComponent
import ru.blays.ficbook.reader.shared.components.themeComponents.ThemeComponent
import ru.blays.ficbook.reader.shared.components.usersComponent.declaration.UsersRootComponent
import ru.blays.ficbook.reader.shared.preferences.json.GlassEffectConfig
import java.util.*

interface RootComponent {

    val childStack: Value<ChildStack<*, Child>>

    val themeComponent: ThemeComponent

    val glassEffectConfig: StateFlow<GlassEffectConfig>

    sealed class Intent {
        data class NewDeepLink(val deepLink: String): Intent()
    }

    fun sendIntent(intent: Intent)

    @Serializable
    sealed class Config {
        @Serializable
        data object Landing: Config()

        @Serializable
        data object Main: Config()

        @Serializable
        data object Settings: Config()

        @Serializable
        data class UserProfile(val initialConfiguration: UserProfileRootComponent.Config): Config()

        @Serializable
        data class FanficPage(
            val href: String,
            private val uuid: String = UUID.randomUUID().toString()
        ): Config()

        @Serializable
        data class FanficsList(
            val section: SectionWithQuery,
            private val uuid: String = UUID.randomUUID().toString()
        ): Config()

        @Serializable
        data class Collection(
            val section: SectionWithQuery,
            private val uuid: String = UUID.randomUUID().toString()
        ): Config()

        @Serializable
        data class AuthorProfile(
            val href: String,
            private val uuid: String = UUID.randomUUID().toString()
        ): Config()

        @Serializable
        data object Users: Config()

        @Serializable
        data object Notifications: Config()

        @Serializable
        data object Search: Config()

        @Serializable
        data object About: Config()
    }

    sealed class Child {
        data class Landing(val component: LandingScreenComponent): Child()
        data class Main(val component: MainScreenComponent): Child()
        data class Settings(val component: SettingsMainComponent): Child()
        data class UserProfile(val component: UserProfileRootComponent): Child()
        data class FanficPage(val component: FanficPageComponent): Child()
        data class FanficsList(val component: FanficsListComponent): Child()
        data class Collection(val component: CollectionFanficsListComponent): Child()
        data class AuthorProfile(val component: AuthorProfileComponent): Child()
        data class Users(val component: UsersRootComponent): Child()
        data class Notifications(val component: NotificationComponent): Child()
        data class Search(val component: SearchComponent): Child()
        data class About(val onBack: () -> Unit) : Child()
    }
}