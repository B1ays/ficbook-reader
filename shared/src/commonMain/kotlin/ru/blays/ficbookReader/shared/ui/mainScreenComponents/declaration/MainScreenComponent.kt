package ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbookReader.shared.data.dto.SavedUserModel
import ru.blays.ficbookReader.shared.data.mappers.toApiModel
import ru.blays.ficbookReader.shared.ui.profileComponents.declaration.UserLogInComponent
import ru.blays.ficbookapi.data.SectionWithQuery

interface MainScreenComponent {
    val tabs: Array<TabModel>
    val state: StateFlow<SavedUserModel?>

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
        data object OpenNotifications: Output()
        data object Search : Output()
    }

    data class TabModel(
        val index: Int,
        val name: String
    )
}