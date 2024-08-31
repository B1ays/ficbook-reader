package ru.blays.ficbook.reader.shared.components.profileComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.blays.ficbook.api.dataModels.LoginModel
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.components.profileComponents.declaration.UserLogInComponent
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbook.reader.shared.platformUtils.runOnUiThread
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.settings

class DefaultUserLogInComponent(
    componentContext: ComponentContext,
    private val output: (output: UserLogInComponent.Output) -> Unit
): UserLogInComponent, ComponentContext by componentContext, KoinComponent {
    private val repository: IAuthorizationRepo by inject()

    private val _state: MutableValue<UserLogInComponent.State> = MutableValue(UserLogInComponent.State())
    override val state get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendIntent(intent: UserLogInComponent.Intent) {
        when(intent) {
            is UserLogInComponent.Intent.LogIn -> logIn(
                login = state.value.login,
                password = state.value.password
            )
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
            val result = repository.addNewUser(
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
                        settings.putBoolean(
                            key = SettingsKeys.FIRST_START_KEY,
                            value = false
                        )
                        runOnUiThread {
                            onOutput(
                                UserLogInComponent.Output.OpenMainScreen
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


    override fun onOutput(output: UserLogInComponent.Output) {
        this.output.invoke(output)
    }
}