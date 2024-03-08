package ru.blays.ficbook.reader.shared.components.usersComponent.declaration

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

@OptIn(ExperimentalDecomposeApi::class)
interface UsersRootComponent {
    val tabs: Value<ChildPages<TabConfig, Tabs>>

    fun sendIntent(intent: Intent)

    fun onOutput(output: Output)

    sealed class Intent {
        data class SelectTab(val index: Int): Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenAuthorProfile(val href: String): Output()
    }

    sealed class Tabs {
        data class FavouriteAuthors(val component: UsersFavouriteComponent): Tabs()
        data class PopularAuthors(val component: UsersPopularComponent): Tabs()
        data class SearchAuthors(val component: UsersSearchComponent): Tabs()
    }

    @Serializable
    sealed class TabConfig {
        @Serializable
        data object FavouriteAuthors: TabConfig()

        @Serializable
        data object PopularAuthors: TabConfig()

        @Serializable
        data object SearchAuthors: TabConfig()
    }
}