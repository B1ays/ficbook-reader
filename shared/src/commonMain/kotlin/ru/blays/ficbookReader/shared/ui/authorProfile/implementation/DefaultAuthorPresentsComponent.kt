package ru.blays.ficbookReader.shared.ui.authorProfile.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.koin.java.KoinJavaComponent
import ru.blays.ficbookReader.shared.data.repo.declaration.IAuthorProfileRepo
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorPresentsComponent

class DefaultAuthorPresentsComponent(
    componentContext: ComponentContext,
    private val href: String,
    private val output: (output: AuthorPresentsComponent.Output) -> Unit
): AuthorPresentsComponent, ComponentContext by componentContext {
    private val authorProfileRepo: IAuthorProfileRepo by KoinJavaComponent.getKoin().inject()

    private val _state: MutableValue<AuthorPresentsComponent.State> = MutableValue(
        AuthorPresentsComponent.State(
            loading = true,
            error = false,
            errorMessage = null,
            presents = emptyList()
        )
    )
    override val state: Value<AuthorPresentsComponent.State>
        get() = _state

    override fun onOutput(output: AuthorPresentsComponent.Output) {
        this.output(output)
    }
    // TODO load presents
}