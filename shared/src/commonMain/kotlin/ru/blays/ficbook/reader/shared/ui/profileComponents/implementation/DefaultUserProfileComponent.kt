package ru.blays.ficbook.reader.shared.ui.profileComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbook.reader.shared.ui.profileComponents.declaration.UserProfileComponent

class DefaultUserProfileComponent(
    componentContext: ComponentContext,
    private val output: (output: UserProfileComponent.Output) -> Unit
): UserProfileComponent, ComponentContext by componentContext {
    private val authorizationRepo: IAuthorizationRepo by getKoin().inject()

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
        this.output(output)
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}