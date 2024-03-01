package ru.blays.ficbook.reader.shared.ui.authorProfile.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorProfileRepo
import ru.blays.ficbook.reader.shared.ui.authorProfile.declaration.AuthorBlogComponent
import ru.blays.ficbook.reader.shared.ui.authorProfile.declaration.AuthorBlogPageComponent
import ru.blays.ficbook.reader.shared.ui.authorProfile.declaration.AuthorBlogPostsComponent

class DefaultAuthorBlogComponent(
    componentContext: ComponentContext,
    private val userID: String,
    private val output: (output: AuthorBlogComponent.Output) -> Unit
): AuthorBlogComponent, ComponentContext by componentContext {
    private val authorProfileRepo: IAuthorProfileRepo by KoinJavaComponent.getKoin().inject()

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
                navigation.push(
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

class DefaultAuthorBlogPosts(
    componentContext: ComponentContext,
    private val authorProfileRepo: IAuthorProfileRepo,
    private val userID: String,
    private val output: (output: AuthorBlogPostsComponent.Output) -> Unit
): AuthorBlogPostsComponent, ComponentContext by componentContext {
    private val _state: MutableValue<AuthorBlogPostsComponent.State> = MutableValue(
        AuthorBlogPostsComponent.State(
            loading = false,
            error = false,
            errorMessage = null,
            posts = emptyList()
        )
    )
    override val state: Value<AuthorBlogPostsComponent.State>
        get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var nextPage: Int = 1
    private var hasNextPage = true

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
                    _state.update {
                        val posts = it.posts + result.value.list
                        it.copy(
                            loading = false,
                            error = false,
                            posts = posts
                        )
                    }
                    nextPage += 1
                }
            }
        }
    }

    init {
        loadPage(nextPage)
    }
}

class DefaultAuthorBlogPageComponent(
    componentContext: ComponentContext,
    private val authorProfileRepo: IAuthorProfileRepo,
    private val userID: String,
    private val postID: String,
    private val output: (output: AuthorBlogPageComponent.Output) -> Unit
): AuthorBlogPageComponent, ComponentContext by componentContext {
    private val _state: MutableValue<AuthorBlogPageComponent.State> = MutableValue(
        AuthorBlogPageComponent.State(
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
        loadPage()
    }
}