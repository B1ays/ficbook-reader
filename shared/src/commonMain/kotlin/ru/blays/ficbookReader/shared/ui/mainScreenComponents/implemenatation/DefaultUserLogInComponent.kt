package ru.blays.ficbookReader.shared.ui.mainScreenComponents.implemenatation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.realm.kotlin.Realm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.get
import ru.blays.ficbookReader.shared.data.realmModels.CookieEntity
import ru.blays.ficbookReader.shared.platformUtils.runOnUiThread
import ru.blays.ficbookReader.shared.ui.mainScreenComponents.declaration.UserLogInComponent
import ru.blays.ficbookapi.dataModels.LoginModel
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

class DefaultUserLogInComponent(
    componentContext: ComponentContext,
    private val ficbookApi: IFicbookApi,
    private val output: (output: UserLogInComponent.Output) -> Unit
): UserLogInComponent, ComponentContext by componentContext {
    private val _state: MutableValue<UserLogInComponent.State> = MutableValue(UserLogInComponent.State())
    override val state: Value<UserLogInComponent.State>
        get() = _state

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }

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

    private fun logIn(login: String, password: String) {
        coroutineScope.launch {
            val result = ficbookApi.authorize(
                LoginModel(
                    login = login,
                    password = password,
                    remember = true
                )
            )
            if(result.responseResult.success) {
                runOnUiThread {
                    output(
                        UserLogInComponent.Output.LogInSuccess(
                            result.cookies
                        )
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        success = false
                    )
                }
            }
        }
    }

    private fun logOut() {
        coroutineScope.launch {
            ficbookApi.logOut()
            val realm = get<Realm>(Realm::class.java)
            realm.write {
                val savedCookies = query(CookieEntity::class).find()
                delete(savedCookies)
            }
        }
    }

    override fun onOutput(output: UserLogInComponent.Output) {
        this.output.invoke(output)
    }
}