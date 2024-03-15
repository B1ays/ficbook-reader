package ru.blays.ficbook.reader.shared.components.authorProfileComponents.declaration

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.blays.ficbook.reader.shared.data.dto.BlogPostCardModelStable
import ru.blays.ficbook.reader.shared.data.dto.BlogPostModelStable

interface AuthorBlogComponent {

    val childStack: Value<ChildStack<*, Child>>

    sealed class Output {
        data class OpenUrl(val url: String): Output()
    }

    @Serializable
    sealed class Config {
        @Serializable
        data object PostsList: Config()

        @Serializable
        data class PostPage(val postID: String): Config()
    }

    sealed class Child {
        data class PostsList(val component: AuthorBlogPostsComponent): Child()
        data class PostPage(val component: AuthorBlogPageComponent): Child()
    }
}

interface AuthorBlogPostsComponent {
    val state: Value<State>

    fun onOutput(output: Output)
    fun sendIntent(intent: Intent)

    sealed class Intent {
        data object LoadNextPage: Intent()
    }

    sealed class Output {
        data class OpenPostPage(val postID: String): Output()
        data class OpenUrl(val url: String): Output()
    }

    data class State(
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?,
        val posts: List<BlogPostCardModelStable>
    )
}

interface AuthorBlogPageComponent {
    val state: Value<State>

    fun onOutput(output: Output)

    sealed class Output {
        data object NavigateBack: Output()
        data class OpenUrl(val url: String): Output()
    }

    data class State(
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?,
        val post: BlogPostModelStable?
    )
}