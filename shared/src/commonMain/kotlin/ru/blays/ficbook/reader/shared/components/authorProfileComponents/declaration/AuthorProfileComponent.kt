package ru.blays.ficbook.reader.shared.components.authorProfileComponents.declaration

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbook.api.data.SectionWithQuery
import ru.blays.ficbook.reader.shared.components.authorProfileComponents.implementation.DefaultAuthorFollowComponent
import ru.blays.ficbook.reader.shared.components.collectionComponents.declaration.CollectionsListComponent
import ru.blays.ficbook.reader.shared.components.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbook.reader.shared.components.fanficListComponents.declaration.FanficsListComponent
import ru.blays.ficbook.reader.shared.data.dto.AuthorProfileModelStable

@OptIn(ExperimentalDecomposeApi::class)
interface AuthorProfileComponent {
    val state: Value<State>

    val tabs: Value<ChildPages<TabConfig, Tabs>>

    val followComponent: DefaultAuthorFollowComponent

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data object Refresh: Intent()
        data class SelectTabs(val index: Int): Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenUrl(val url: String): Output()
        data class OpenAnotherProfile(val href: String): Output()
        data class OpenFanfic(val href: String): Output()
        data class OpenFanficsList(val section: SectionWithQuery): Output()
        data class OpenCollection(val relativeID: String, val realID: String): Output()
    }

    sealed class Tabs {
        data class Main(val component: AuthorProfileComponent): Tabs()
        data class Blog(val component: AuthorBlogComponent): Tabs()
        data class Works(val component: FanficsListComponent): Tabs()
        data class WorksAsCoauthor(val component: FanficsListComponent): Tabs()
        data class WorksAsBeta(val component: FanficsListComponent): Tabs()
        data class WorksAsGamma(val component: FanficsListComponent): Tabs()
        data class Comments(val component: CommentsComponent): Tabs()
        data class Presents(val component: AuthorPresentsComponent): Tabs()
        data class Collections(val component: CollectionsListComponent): Tabs()
    }

    @Serializable
    sealed class TabConfig {
        @Serializable
        data object Main: TabConfig()
        @Serializable
        data object Blog: TabConfig()
        @Serializable
        data object Works: TabConfig()
        @Serializable
        data object WorksAsCoauthor : TabConfig()
        @Serializable
        data object WorksAsBeta : TabConfig()
        @Serializable
        data object WorksAsGamma : TabConfig()
        @Serializable
        data object Comments : TabConfig()
        @Serializable
        data object Presents : TabConfig()
        @Serializable
        data object Collections: TabConfig()
    }

    @Serializable
    data class State(
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?,
        val profile: AuthorProfileModelStable?,
        val availableTabs: List<TabConfig>,
    )
}