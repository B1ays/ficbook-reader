package ru.blays.ficbookReader.shared.data.repo.declaration

import kotlinx.coroutines.flow.StateFlow
import okhttp3.CookieJar
import ru.blays.ficbookReader.shared.data.dto.UserModelStable
import ru.blays.ficbookapi.dataModels.AuthorizationResult
import ru.blays.ficbookapi.dataModels.LoginModel
import ru.blays.ficbookapi.result.ApiResult

interface IAuthorizationRepo {
    val authorized: StateFlow<Boolean>
    val currentUser: StateFlow<UserModelStable?>

    val cookieStorage: CookieJar

    suspend fun logIn(loginModel: LoginModel): ApiResult<AuthorizationResult>
    suspend fun logOut()
}