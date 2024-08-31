package ru.blays.ficbook.reader.shared.data.repo.declaration

import kotlinx.coroutines.flow.StateFlow
import ru.blays.ficbook.api.dataModels.AuthorizationResult
import ru.blays.ficbook.api.dataModels.LoginModel
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.dto.SavedUserModel

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

    fun switchAnonymousMode(enabled: Boolean)

    suspend fun migrateDB(): Boolean

    fun initialize()
}