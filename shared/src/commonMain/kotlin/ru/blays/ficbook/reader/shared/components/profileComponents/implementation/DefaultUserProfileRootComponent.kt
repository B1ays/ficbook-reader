package ru.blays.ficbook.reader.shared.components.profileComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserLogInComponent
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileComponent
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileManagingComponent
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileRootComponent

class DefaultUserProfileRootComponent(
    componentContext: ComponentContext,
    initialConfiguration: UserProfileRootComponent.Config,
    private val output: (output: UserProfileRootComponent.Output) -> Unit
): UserProfileRootComponent, ComponentContext by componentContext {
    private val navigation = StackNavigation<UserProfileRootComponent.Config>()

    override val childStack = childStack(
        source = navigation,
        initialConfiguration = initialConfiguration,
        handleBackButton = true,
        serializer = UserProfileRootComponent.Config.serializer(),
        childFactory = ::childFactory
    )

    override fun onOutput(output: UserProfileRootComponent.Output) {
        this.output(output)
    }

    private fun childFactory(config: UserProfileRootComponent.Config, childContext: ComponentContext): UserProfileRootComponent.Child {
        return when(config) {
            UserProfileRootComponent.Config.LogIn -> UserProfileRootComponent.Child.AddAccount(
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
            UserProfileRootComponent.Config.AccountManaging -> UserProfileRootComponent.Child.AccountManaging(
                DefaultUserProfileManagingComponent(
                    componentContext = childContext,
                    output = ::onManagingOutput
                )
            )
        }
    }
    
    private fun onLogInOutput(output: UserLogInComponent.Output) {
        when(output) {
            is UserLogInComponent.Output.NavigateBack -> onOutput(
                UserProfileRootComponent.Output.NavigateBack
            )
            is UserLogInComponent.Output.OpenMainScreen -> onOutput(
                UserProfileRootComponent.Output.OpenMainScreen
            )
        }
    }

    private fun onProfileOutput(output: UserProfileComponent.Output) {
        when(output) {
            is UserProfileComponent.Output.OpenProfile -> onOutput(
                UserProfileRootComponent.Output.OpenProfile(output.href)
            )
            is UserProfileComponent.Output.NavigateBack -> navigation.pop { success ->
                if(!success) onOutput(
                    UserProfileRootComponent.Output.NavigateBack
                )
            }
            UserProfileComponent.Output.ManageAccounts -> {
                navigation.push(UserProfileRootComponent.Config.AccountManaging)
            }
        }
    }

    private fun onManagingOutput(output: UserProfileManagingComponent.Output) {
        when(output) {
            UserProfileManagingComponent.Output.AddNewAccount -> {
                navigation.push(UserProfileRootComponent.Config.LogIn)
            }
            UserProfileManagingComponent.Output.NavigateBack -> {
                navigation.pop()
            }
        }
    }
}