package ru.blays.ficbookReader.shared.ui.profileComponents

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent
import ru.blays.ficbookReader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbookReader.shared.platformUtils.runOnUiThread
import ru.blays.ficbookapi.dataModels.LoginModel
import ru.blays.ficbookapi.result.ApiResult

class DefaultUserLogInComponent(
    componentContext: ComponentContext,
    private val output: (output: UserLogInComponent.Output) -> Unit
): UserLogInComponent, ComponentContext by componentContext {
    private val repository: IAuthorizationRepo by KoinJavaComponent.getKoin().inject()

    private val _state: MutableValue<UserLogInComponent.State> = MutableValue(UserLogInComponent.State())
    override val state get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: UserLogInComponent.Intent) {
        when(intent) {
            is UserLogInComponent.Intent.LogIn -> logIn(
                login = state.value.login,
                password = state.value.password
            )
            is UserLogInComponent.Intent.LogOut -> logOut()
            is UserLogInComponent.Intent.LoginChanged -> {
                _state.update {
                    it.copy(
                        login = intent.login
                    )
                }
            }
            is UserLogInComponent.Intent.PasswordChanged -> {
                _state.update {
                    it.copy(
                        password = intent.password
                    )
                }
            }
        }
    }

    private fun logIn(
        login: String,
        password: String
    ) {
        coroutineScope.launch {
            val result = repository.logIn(
                LoginModel(
                    login = login,
                    password = password,
                    remember = true
                )
            )
            when(result) {
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            success = false,
                            reason = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    if(result.value.responseResult.success) {
                        runOnUiThread {
                            onOutput(
                                UserLogInComponent.Output.NavigateBack
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                success = false,
                                reason = result.value.responseResult.error?.reason
                            )
                        }
                    }
                }
            }
        }
    }

    private fun logOut() = coroutineScope.launch {
        repository.logOut()
    }

    override fun onOutput(output: UserLogInComponent.Output) {
        this.output.invoke(output)
    }
}