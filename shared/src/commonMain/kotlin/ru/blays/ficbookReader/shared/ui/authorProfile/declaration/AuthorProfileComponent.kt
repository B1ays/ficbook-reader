package ru.blays.ficbookReader.shared.ui.authorProfile.declaration

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbookReader.shared.data.dto.AuthorProfileModelStable
import ru.blays.ficbookReader.shared.ui.commentsComponent.declaration.CommentsComponent
import ru.blays.ficbookReader.shared.ui.fanficListComponents.FanficsListComponent
import ru.blays.ficbookapi.data.SectionWithQuery

@OptIn(ExperimentalDecomposeApi::class)
interface AuthorProfileComponent {
    val state: Value<State>

    val tabs: Value<ChildPages<TabConfig, Tabs>>

    fun sendIntent(intent: Intent)
    fun onOutput(output: Output)

    sealed class Intent {
        data object Refresh: Intent()
        data class Follow(val follow: Boolean): Intent()

        data class SelectTabs(val index: Int): Intent()
    }

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenUrl(val url: String): Output()
        data class OpenAnotherProfile(val href: String): Output()
        data class OpenFanfic(val href: String): Output()
        data class OpenFanficsList(val section: SectionWithQuery): Output()
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
    }

    @Serializable
    sealed class TabConfig {
        @Serializable
        data object Main: TabConfig()
        @Serializable
        data class Blog(val userID: String): TabConfig()
        @Serializable
        data class Works(val section: SectionWithQuery): TabConfig()
        @Serializable
        data class WorksAsCoauthor(val section: SectionWithQuery): TabConfig()
        @Serializable
        data class WorksAsBeta(val section: SectionWithQuery): TabConfig()
        @Serializable
        data class WorksAsGamma(val section: SectionWithQuery): TabConfig()
        @Serializable
        data class Comments(val userID: String): TabConfig()
        @Serializable
        data class Presents(val href: String): TabConfig()
    }

    data class State(
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?,
        val profile: AuthorProfileModelStable?,
        val availableTabs: List<TabConfig>,
    )
}