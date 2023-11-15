package ru.blays.ficbookReader.shared.ui.authorProfile.implementation

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
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorBlogComponent
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorBlogPageComponent
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorBlogPostsComponent
import ru.blays.ficbookapi.dataModels.BlogPostCardModel
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi
import ru.blays.ficbookapi.result.ApiResult

class DefaultAuthorBlogComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    href: String,
    private val output: (output: AuthorBlogComponent.Output) -> Unit
): AuthorBlogComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<AuthorBlogComponent.Config>()
    override val childStack = childStack(
        source = navigation,
        serializer = AuthorBlogComponent.Config.serializer(),
        initialConfiguration = AuthorBlogComponent.Config.PostsList(href),
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
                    ficbookApi = ficbookApi,
                    href = config.href,
                    output = ::pageOutput
                )
            )
            is AuthorBlogComponent.Config.PostsList -> AuthorBlogComponent.Child.PostsList(
                DefaultAuthorBlogPosts(
                    componentContext = componentContext,
                    ficbookApi = ficbookApi,
                    href = config.href,
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
                        href = output.href
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
    private val ficbookApi: IFicbookApi,
    private val href: String,
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
            val result = ficbookApi.getAuthorBlogPosts(
                href = href,
                page = page
            )
            when(result) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = true,
                            errorMessage = result.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    _state.update {
                        val posts = it.posts + result.value.map(BlogPostCardModel::toStableModel)
                        it.copy(
                            loading = false,
                            error = false,
                            posts = posts
                        )
                    }
                    this@DefaultAuthorBlogPosts.nextPage += 1
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
    private val ficbookApi: IFicbookApi,
    private val href: String,
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
        val result = ficbookApi.getAuthorBlogPost(href = href)
        when(result) {
            is ApiResult.Error -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = true,
                        errorMessage = result.message
                    )
                }
            }
            is ApiResult.Success -> {
                _state.update {
                    it.copy(
                        loading = false,
                        error = false,
                        post = result.value.toStableModel()
                    )
                }
            }
        }
    }

    init {
        loadPage()
        /*val fakePage = BlogPostPageModelStable(
            title = "Test post",
            date = "Test date",
            text = testText,
            likes = 10
        )
        _state.update {
            it.copy(
                loading = false,
                error = false,
                post = fakePage
            )
        }*/
    }

}

const val testText = """
    Ну что, любимые мои дорогие читатели?
Уже почти Новый год. А это значит что? Это значит, что я начинаю выкладку подарков. Первый уже выложен, "Семейное дело" по осточертевшим Сумеркам, которые я "не смотрела, но осуждаю". Также будут обновки по Флагу, Норе и даже Крови. Сейчас они чистятся, полируются и дописываются. Думаю, к новогодней ночи успею что-то еще выложить.

Ну и конечно, поздравляю с наступающим Новым годом всех своих читателей!
Вы меня поддерживали и толкали в сторону ноутбука весь этот тяжелый год, ждали и верили в меня. Спасибо ВАМ ВСЕМ. Благодаря вам я пережила этот год и не поехала крышей. Спасибо!

Пусть у нас всех все будет хорошо.
"""