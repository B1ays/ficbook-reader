package ru.blays.ficbook.reader.shared.components.profileComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserProfileManagingComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo

class DefaultUserProfileManagingComponent(
    componentContext: ComponentContext,
    private val output: (UserProfileManagingComponent.Output) -> Unit
): UserProfileManagingComponent, ComponentContext by componentContext, KoinComponent {
    private val repository: IAuthorizationRepo by inject()

    private val _state = MutableValue(
        UserProfileManagingComponent.State(
            savedUsers = emptyList(),
            selectedUserID = repository.selectedUserID
        )
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val state = _state

    override fun sendIntent(intent: UserProfileManagingComponent.Intent) {
        when(intent) {
            is UserProfileManagingComponent.Intent.AddNewAccount -> {
                output(UserProfileManagingComponent.Output.AddNewAccount)
            }
            is UserProfileManagingComponent.Intent.ChangeUser -> coroutineScope.launch {
                repository.changeCurrentUser(intent.id)
                _state.update {
                    it.copy(
                        selectedUserID = intent.id
                    )
                }
            }
            is UserProfileManagingComponent.Intent.DeleteUser -> coroutineScope.launch {
                removeUser(intent.id)
            }
        }
    }

    override fun onOutput(output: UserProfileManagingComponent.Output) = this.output(output)
    
    private fun getSavedAccounts() {
        coroutineScope.launch {
            val users = repository.getAllUsers()
            _state.update {
                it.copy(
                    savedUsers = users
                )
            }
        }
    }

    private suspend fun removeUser(id: String) {
        repository.removeUser(id)
        _state.update {
            it.copy(
                savedUsers = it.savedUsers.filter { it.id != id }
            )
        }
    }

    init {
        getSavedAccounts()
    }
}