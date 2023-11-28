package ru.blays.ficbookReader.shared.data.repo.implementation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.CookieJar
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.cookieStorage.DynamicCookieJar
import ru.blays.ficbookReader.shared.data.dto.UserModelStable
import ru.blays.ficbookReader.shared.data.mappers.toStableModel
import ru.blays.ficbookReader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbookapi.api.AuthorizationApi
import ru.blays.ficbookapi.dataModels.AuthorizationResult
import ru.blays.ficbookapi.dataModels.LoginModel
import ru.blays.ficbookapi.result.ApiResult

class AuthorizationRepo(
    private val api: AuthorizationApi
): IAuthorizationRepo {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _authorized: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _currentUser: MutableStateFlow<UserModelStable?> = MutableStateFlow(null)
    private val _cookieStorage: DynamicCookieJar by getKoin().inject()

    override val authorized get() = _authorized
    override val currentUser get() = _currentUser
    override val cookieStorage: CookieJar
        get() = _cookieStorage

    override suspend fun logIn(loginModel: LoginModel): ApiResult<AuthorizationResult> {
        val result = api.logIn(loginModel)
        if(result is ApiResult.Success) {
            _currentUser.value = result.value.user?.toStableModel()
            _authorized.value = true
        }
        return result
    }

    override suspend fun logOut() {
        _currentUser.value = null
        _authorized.value = false
        _cookieStorage.clearAll()
    }

    init {
        coroutineScope.launch {
            val checkResult = api.checkAuthorization()
            if(checkResult is ApiResult.Success) {
                _authorized.value = true
                _currentUser.value = checkResult.value.toStableModel()
            }
        }
    }
}