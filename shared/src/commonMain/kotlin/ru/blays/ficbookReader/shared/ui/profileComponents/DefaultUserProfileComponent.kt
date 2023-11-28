package ru.blays.ficbookReader.shared.ui.profileComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbookReader.shared.platformUtils.runOnUiThread

class DefaultUserProfileComponent(
    componentContext: ComponentContext,
    private val output: (output: UserProfileComponent.Output) -> Unit
): UserProfileComponent, ComponentContext by componentContext {
    private val authorizationRepo: IAuthorizationRepo by getKoin().inject()

    override val state get() = authorizationRepo.currentUser

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: UserProfileComponent.Intent) {
        when(intent) {
            UserProfileComponent.Intent.LogOut -> {
                coroutineScope.launch {
                    authorizationRepo.logOut()
                    runOnUiThread {
                        onOutput(
                            UserProfileComponent.Output.NavigateBack
                        )
                    }
                }
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