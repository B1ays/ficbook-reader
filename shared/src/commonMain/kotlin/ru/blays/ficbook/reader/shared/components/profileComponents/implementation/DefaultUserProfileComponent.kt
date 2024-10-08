package ru.blays.ficbook.reader.shared.components.profileComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo

class DefaultUserProfileComponent(
    componentContext: ComponentContext,
    private val output: (output: UserProfileComponent.Output) -> Unit
): UserProfileComponent, ComponentContext by componentContext, KoinComponent {
    private val authorizationRepo: IAuthorizationRepo by inject()

    override val state get() = authorizationRepo.currentUserModel

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: UserProfileComponent.Intent) {
        when(intent) {
            UserProfileComponent.Intent.EnableIncognito -> {
                authorizationRepo.switchAnonymousMode(true)
            }
        }
    }

    override fun onOutput(output: UserProfileComponent.Output) {
        when(output) {
            is UserProfileComponent.Output.OpenProfile -> {
                state.value?.let {
                    output.href = "authors/${it.id}"
                    this.output(output)
                }
            }
            else -> this.output(output)
        }
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}