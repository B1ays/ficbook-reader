package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.Section
import ru.blays.ficbookReader.shared.data.dto.UserModelStable
import ru.blays.ficbookReader.shared.data.mappers.toApiModel
import ru.blays.ficbookReader.shared.data.sections.userSections
import ru.blays.ficbookapi.data.SectionWithQuery

interface MainScreenComponent {
    val tabs: Array<TabModel>
    val state: Value<State>
    /*val childStack: Value<ChildStack<*, Child>>*/

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    val feedComponent: FeedComponent
    val popularSectionsComponent: PopularSectionsComponent
    val collectionsComponent: CollectionsComponent
    val savedFanficsComponent: SavedFanficsComponent
    val logInComponent: UserLogInComponent

    sealed class Output {
        data class OpenFanficsList(val sectionWithQuery: SectionWithQuery): Output() {
            constructor(
                section: Section
            ) : this(
                sectionWithQuery = SectionWithQuery(
                    name = section.name,
                    path = section.segments,
                    queryParameters = emptyList()
                )
            )
            constructor(
                sectionWithQuery: ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
            ) : this(
                sectionWithQuery = sectionWithQuery.toApiModel()
            )
        }
        data class OpenFanficPage(val href: String): Output()
        data object OpenRandomFanficPage: Output()
        data object UserButtonClicked: Output()
    }

    sealed class Intent {
        data object Login: Intent()
    }

    /*sealed class Child {
        data class FanficPage(val component: FanficPageComponent): Child()
    }

    @Serializable
    sealed class Config {
        @Serializable
        data object MainPage: Config()
        @Serializable
        data class FanficPage(val href: String): Config()
    }*/

    data class State(
        val mainFeed: SectionWithQuery = userSections.follow.toApiModel(),
        val user: UserModelStable? = null,
        val authorized: Boolean = false,
    )

    data class TabModel(
        val index: Int,
        val name: String
    )
}