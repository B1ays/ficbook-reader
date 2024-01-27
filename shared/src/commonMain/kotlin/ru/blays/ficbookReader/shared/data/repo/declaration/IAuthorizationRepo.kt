package ru.blays.ficbookReader.shared.data.repo.declaration

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbookReader.shared.data.dto.SavedUserModel
import ru.blays.ficbookapi.dataModels.AuthorizationResult
import ru.blays.ficbookapi.dataModels.LoginModel
import ru.blays.ficbookapi.result.ApiResult

interface IAuthorizationRepo {
    val currentUserModel: StateFlow<SavedUserModel?>
    val selectedUserID: String?

    val hasSavedAccount: Boolean
    val hasSavedCookies: Boolean
    val anonymousMode: Boolean

    suspend fun addNewUser(loginModel: LoginModel): ApiResult<AuthorizationResult>
    suspend fun changeCurrentUser(id: String): Boolean
    suspend fun removeUser(id: String)

    suspend fun getAllUsers(): List<SavedUserModel>

    fun switchAnonymousMode(enable: Boolean)

    suspend fun migrateDB(): Boolean

    fun initialize()
}