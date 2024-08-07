package ru.blays.ficbook.reader.shared.components.mainScreenComponents.declaration

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponent
import ru.blays.ficbook.reader.shared.components.collectionComponents.implementation.EditCollectionDialogConfig
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserLogInComponent
import ru.blays.ficbook.reader.shared.data.dto.SavedUserModel
import ru.blays.ficbook.reader.shared.data.mappers.toApiModel

interface MainScreenComponent {
    val tabs: Array<TabModel>
    val state: StateFlow<SavedUserModel?>

    fun onOutput(output: Output)

    val feedComponent: FeedComponent
    val popularSectionsComponent: PopularSectionsComponent
    val collectionsComponent: CollectionsListComponent
    val savedFanficsComponent: SavedFanficsComponent
    val logInComponent: UserLogInComponent

    sealed class Output {
        data class OpenFanficsList(val sectionWithQuery: SectionWithQuery): Output() {
            constructor(
                sectionWithQuery: ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery
            ) : this(
                sectionWithQuery = sectionWithQuery.toApiModel()
            )
        }
        data class OpenCollection(
            val relativeID: String,
            val realID: String,
            val initialDialogConfig: EditCollectionDialogConfig?
        ): Output()
        data class OpenFanficPage(val href: String): Output()
        data class OpenUrl(val url: String) : Output()
        data class OpenAuthor(val href: String) : Output()
        data object OpenRandomFanficPage: Output()
        data object OpenSettings: Output()
        data object OpenAbout: Output()
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