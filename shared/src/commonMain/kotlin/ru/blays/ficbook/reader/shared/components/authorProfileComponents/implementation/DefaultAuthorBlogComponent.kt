package ru.blays.ficbook.reader.shared.components.authorProfileComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.doOnStart
import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.saveable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.serializer
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.authorProfileComponents.declaration.AuthorBlogComponent
import ru.blays.ficbook.reader.shared.components.authorProfileComponents.declaration.AuthorBlogPageComponent
import ru.blays.ficbook.reader.shared.components.authorProfileComponents.declaration.AuthorBlogPostsComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorProfileRepo
import ru.blays.ficbook.reader.shared.stateHandle.SaveableMutableValue

class DefaultAuthorBlogComponent(
    componentContext: ComponentContext,
    private val userID: String,
    private val output: (output: AuthorBlogComponent.Output) -> Unit
): AuthorBlogComponent, ComponentContext by componentContext, KoinComponent {
    private val authorProfileRepo: IAuthorProfileRepo by inject()

    private val navigation = StackNavigation<AuthorBlogComponent.Config>()
    override val childStack = childStack(
        source = navigation,
        serializer = AuthorBlogComponent.Config.serializer(),
        initialConfiguration = AuthorBlogComponent.Config.PostsList,
        handleBackButton = true,
        childFactory = ::childFactory
    )

    private fun childFactory(
        config: AuthorBlogComponent.Config,
        componentContext: ComponentContext
    ): AuthorBlogComponent.Child {
        return when (config) {
            is AuthorBlogComponent.Config.PostPage -> AuthorBlogComponent.Child.PostPage(
                DefaultAuthorBlogPageComponent(
                    componentContext = componentContext,
                    authorProfileRepo = authorProfileRepo,
                    userID = userID,
                    postID = config.postID,
                    output = ::pageOutput
                )
            )
            is AuthorBlogComponent.Config.PostsList -> AuthorBlogComponent.Child.PostsList(
                DefaultAuthorBlogPosts(
                    componentContext = componentContext,
                    authorProfileRepo = authorProfileRepo,
                    userID = userID,
                    output = ::listOutput
                )
            )
        }
    }

    private fun listOutput(output: AuthorBlogPostsComponent.Output) {
        when(output) {
            is AuthorBlogPostsComponent.Output.OpenUrl -> {
                this.output(
                    AuthorBlogComponent.Output.OpenUrl(
                        url = output.url
                    )
                )
            }
            is AuthorBlogPostsComponent.Output.OpenPostPage -> {
                navigation.pushNew(
                    AuthorBlogComponent.Config.PostPage(
                        postID = output.postID
                    )
                )
            }
        }
    }

    private fun pageOutput(output: AuthorBlogPageComponent.Output) {
        when(output) {
            AuthorBlogPageComponent.Output.NavigateBack -> navigation.pop()
            is AuthorBlogPageComponent.Output.OpenUrl -> {
                this.output(
                    AuthorBlogComponent.Output.OpenUrl(
                        url = output.url
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalStateKeeperApi::class)
class DefaultAuthorBlogPosts(
    componentContext: ComponentContext,
    private val authorProfileRepo: IAuthorProfileRepo,
    private val userID: String,
    private val output: (output: AuthorBlogPostsComponent.Output) -> Unit
): AuthorBlogPostsComponent, ComponentContext by componentContext {
    private val _state: MutableValue<AuthorBlogPostsComponent.State> = SaveableMutableValue(
        serializer = AuthorBlogPostsComponent.State.serializer(),
        initialValue = AuthorBlogPostsComponent.State(
            loading = false,
            error = false,
            errorMessage = null,
            posts = emptyList()
        )
    )
    override val state: Value<AuthorBlogPostsComponent.State>
        get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var hasNextPage by saveable(
        serializer = Boolean.serializer(),
        key = HAS_NEXT_PAGE_KEY,
        init = { true }
    )
    private var nextPage: Int by saveable(
        serializer = Int.serializer(),
        key = NEXT_PAGE_KEY,
        init = { 1 }
    )

    override fun onOutput(output: AuthorBlogPostsComponent.Output) {
        this.output(output)
    }

    override fun sendIntent(intent: AuthorBlogPostsComponent.Intent) {
        when(intent) {
            AuthorBlogPostsComponent.Intent.LoadNextPage -> loadPage(nextPage)
        }
    }

    private fun loadPage(page: Int) = coroutineScope.launch {
        if(!state.value.loading) {
            _state.update {
                it.copy(
                    loading = true
                )
            }
            val result = authorProfileRepo.getBlogPosts(
                id = userID,
                page = page
            )
            when(result) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = true,
                            errorMessage = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    hasNextPage = result.value.hasNextPage
                    nextPage++
                    _state.update {
                        val posts = it.posts + result.value.list
                        it.copy(
                            loading = false,
                            error = false,
                            posts = posts
                        )
                    }
                }
            }
        }
    }

    init {
        lifecycle.doOnStart(true) {
            val state = state.value
            if(state.posts.isEmpty() && !state.error) {
                loadPage(nextPage)
            }
        }
    }

    companion object {
        private const val HAS_NEXT_PAGE_KEY = "has_next_page"
        private const val NEXT_PAGE_KEY = "next_page"
    }
}

class DefaultAuthorBlogPageComponent(
    componentContext: ComponentContext,
    private val authorProfileRepo: IAuthorProfileRepo,
    private val userID: String,
    private val postID: String,
    private val output: (output: AuthorBlogPageComponent.Output) -> Unit
): AuthorBlogPageComponent, ComponentContext by componentContext {
    private val _state: MutableValue<AuthorBlogPageComponent.State> = SaveableMutableValue(
        serializer = AuthorBlogPageComponent.State.serializer(),
        initialValue = AuthorBlogPageComponent.State(
            loading = true,
            error = false,
            errorMessage = null,
            post = null
        )
    )
    override val state: Value<AuthorBlogPageComponent.State>
        get() = _state

    val coroutineScope = CoroutineScope(Dispatchers.IO)
    override fun onOutput(output: AuthorBlogPageComponent.Output) {
        this.output(output)
    }

    private fun loadPage() = coroutineScope.launch {
        // TODO realize load blog post
        _state.update {
            it.copy(
                loading = true
            )
        }
        val result = authorProfileRepo.getBlogPage(
            userID = userID,
            postID = postID
        )
        when(result) {
            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = true,
                        errorMessage = result.exception.message
                    )
                }
            }
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = false,
                        post = result.value
                    )
                }
            }
        }
    }

    init {
        lifecycle.doOnStart(true) {
            val state = _state.value
            if(state.post == null && !state.error) {
                loadPage()
            }
        }
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}