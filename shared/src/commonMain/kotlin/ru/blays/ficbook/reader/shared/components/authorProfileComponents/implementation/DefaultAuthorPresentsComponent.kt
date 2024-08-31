package ru.blays.ficbook.reader.shared.components.authorProfileComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.reader.shared.components.authorProfileComponents.declaration.AuthorPresentsComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorProfileRepo

class DefaultAuthorPresentsComponent(
    componentContext: ComponentContext,
    private val href: String,
    private val output: (output: AuthorPresentsComponent.Output) -> Unit
): AuthorPresentsComponent, ComponentContext by componentContext, KoinComponent {
    private val authorProfileRepo: IAuthorProfileRepo by inject()

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