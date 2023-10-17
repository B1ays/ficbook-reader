package ru.blays.ficbookReader.shared.ui.RootComponent

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookReader.shared.ui.fanficPageComponents.declaration.FanficPageComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.MainScreenComponent
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.UserLogInComponent
import ru.blays.ficbookReader.shared.ui.settingsComponents.SettingsMainComponent
import ru.blays.ficbookapi.data.SectionWithQuery

interface RootComponent {

    val childStack: Value<ChildStack<*, Child>>

    @Serializable
    sealed class Config {
        @Serializable
        data object Main: Config()
        @Serializable
        data object Settings: Config()

        @Serializable
        data object Login: Config()

        @Serializable
        data class FanficPage(val href: String): Config()

        @Serializable
        data class FanficsList(val section: SectionWithQuery): Config()
    }

    sealed class Child {
        data class Main(val component: MainScreenComponent): Child()
        data class Settings(val component: SettingsMainComponent): Child()
        data class Login(val component: UserLogInComponent): Child()
        data class FanficPage(val component: FanficPageComponent): Child()
        data class FanficsList(val component: FanficsListComponent): Child()
    }
}