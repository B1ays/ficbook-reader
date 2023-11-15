package ru.blays.ficbookReader.shared.ui.authorProfile.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.ui.authorProfile.declaration.AuthorPresentsComponent
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultAuthorPresentsComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    private val href: String,
    private val output: (output: AuthorPresentsComponent.Output) -> Unit
): AuthorPresentsComponent, ComponentContext by componentContext {
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