package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.UserModelStable
import ru.blays.ficbookReader.shared.data.mappers.toApiModel
import ru.blays.ficbookReader.shared.data.sections.userSections
import ru.blays.ficbookReader.shared.ui.profileComponents.UserLogInComponent
import ru.blays.ficbookapi.data.SectionWithQuery

interface MainScreenComponent {
    val tabs: Array<TabModel>
    val state: Value<State>

    fun onOutput(output: Output)

    val feedComponent: FeedComponent
    val popularSectionsComponent: PopularSectionsComponent
    val collectionsComponent: CollectionsComponent
    val savedFanficsComponent: SavedFanficsComponent
    val logInComponent: UserLogInComponent

    sealed class Output {
        data class OpenFanficsList(val sectionWithQuery: SectionWithQuery): Output() {
            constructor(
                sectionWithQuery: ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
            ) : this(
                sectionWithQuery = sectionWithQuery.toApiModel()
            )
        }
        data class OpenCollection(val section: SectionWithQuery): Output()
        data class OpenFanficPage(val href: String): Output()
        data class OpenUrl(val url: String) : Output()
        data class OpenAuthor(val href: String) : Output()
        data object OpenRandomFanficPage: Output()
        data object OpenSettings: Output()
        data object UserProfile: Output()
        data object OpenUsersScreen: Output()
    }

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