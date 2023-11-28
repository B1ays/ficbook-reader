package ru.blays.ficbookReader.shared.ui.profileComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.repo.declaration.IAuthorizationRepo

class DefaultUserProfileRootComponent(
    componentContext: ComponentContext,
    private val output: (output: UserProfileRootComponent.Output) -> Unit
): UserProfileRootComponent, ComponentContext by componentContext {
    private val authorizationRepo: IAuthorizationRepo by getKoin().inject()
    private val navigation = StackNavigation<UserProfileRootComponent.Config>()

    override val childStack = childStack(
        source = navigation,
        initialConfiguration = if(authorizationRepo.authorized.value) {
            UserProfileRootComponent.Config.Profile
        } else {
            UserProfileRootComponent.Config.LogIn
        },
        handleBackButton = true,
        serializer = UserProfileRootComponent.Config.serializer(),
        childFactory = ::childFactory
    )

    override fun onOutput(output: UserProfileRootComponent.Output) {
        this.output(output)
    }

    private fun childFactory(config: UserProfileRootComponent.Config, childContext: ComponentContext): UserProfileRootComponent.Child {
        return when(config) {
            UserProfileRootComponent.Config.LogIn -> UserProfileRootComponent.Child.LogIn(
                DefaultUserLogInComponent(
                    componentContext = childContext,
                    output = ::onLogInOutput
                )
            )
            UserProfileRootComponent.Config.Profile -> UserProfileRootComponent.Child.Profile(
                DefaultUserProfileComponent(
                    componentContext = childContext,
                    output = ::onProfileOutput
                )
            )
        }
    }
    
    private fun onLogInOutput(output: UserLogInComponent.Output) {
        when(output) {
            is UserLogInComponent.Output.NavigateBack -> onOutput(
                UserProfileRootComponent.Output.NavigateBack
            )
        }
    }

    private fun onProfileOutput(output: UserProfileComponent.Output) {
        when(output) {
            is UserProfileComponent.Output.OpenProfile -> onOutput(
                UserProfileRootComponent.Output.OpenProfile(output.href)
            )
            is UserProfileComponent.Output.NavigateBack -> onOutput(
                UserProfileRootComponent.Output.NavigateBack
            )
        }
    }
}