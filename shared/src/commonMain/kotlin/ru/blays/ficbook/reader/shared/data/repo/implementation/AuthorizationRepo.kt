package ru.blays.ficbook.reader.shared.data.repo.implementation

import com.russhwolf.settings.boolean
import com.russhwolf.settings.nullableString
import io.realm.kotlin.ext.realmListOf
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Cookie
import ru.blays.ficbook.api.api.AuthorizationApi
import ru.blays.ficbook.api.dataModels.AuthorizationResult
import ru.blays.ficbook.api.dataModels.LoginModel
import ru.blays.ficbook.api.dataModels.UserModel
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.cookieStorage.DynamicCookieJar
import ru.blays.ficbook.reader.shared.data.cookieStorage.toCookie
import ru.blays.ficbook.reader.shared.data.cookieStorage.toEntity
import ru.blays.ficbook.reader.shared.data.dto.SavedUserModel
import ru.blays.ficbook.reader.shared.data.dto.toSavedUserModel
import ru.blays.ficbook.reader.shared.data.realm.entity.CookieEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.UserEntity
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorizationRepo
import ru.blays.ficbook.reader.shared.di.getRealm
import ru.blays.ficbook.reader.shared.platformUtils.downloadImageToFile
import ru.blays.ficbook.reader.shared.platformUtils.getFilesDir
import ru.blays.ficbook.reader.shared.preferences.SettingsKeys
import ru.blays.ficbook.reader.shared.preferences.settings
import java.io.File

class AuthorizationRepo(
    private val api: AuthorizationApi,
    private val cookieStorage: DynamicCookieJar
): IAuthorizationRepo {
    private val _currentUserModel: MutableStateFlow<SavedUserModel?> = MutableStateFlow(null)
    private var _selectedUserID: String? by settings.nullableString(
        key = SettingsKeys.ACTIVE_USER_ID_KEY
    )

    override val currentUserModel = _currentUserModel
    override val selectedUserID get() = _selectedUserID
    override val hasSavedAccount: Boolean
        get() = getRealm().query(UserEntity::class).find().isNotEmpty()
    override val hasSavedCookies: Boolean
        get() = getRealm().query(CookieEntity::class).find().isNotEmpty()

    override var anonymousMode by settings.boolean(
        key = SettingsKeys.ANONYMOUS_MODE_KEY,
        defaultValue = false
    )
        private set

    override suspend fun addNewUser(loginModel: LoginModel): ApiResult<AuthorizationResult> {
        return when(
            val result = api.logIn(loginModel)
        ) {
            is ApiResult.Error -> {
                // TODO add message to log
                ApiResult.Error(result.exception)
            }
            is ApiResult.Success -> {
                val userEntity = saveUserToDB(result.value.user)
                val savedUser = userEntity?.toSavedUserModel()
                if(savedUser != null) {
                    _currentUserModel.value = savedUser
                    _selectedUserID = savedUser.id
                    ApiResult.success(result.value)
                } else {
                    ApiResult.failure(Exception("Failed to save user"))
                }
            }
        }
    }

    override suspend fun changeCurrentUser(id: String): Boolean {
        val savedUser = getRealm().query(
            UserEntity::class,
            "id = $0",
            id
        )
        .first()
        .find()

        if(savedUser == null) return false
        _currentUserModel.value = savedUser.toSavedUserModel()
        _selectedUserID = savedUser.id
        cookieStorage.addAll(
            savedUser.cookies.map(CookieEntity::toCookie)
        )
        anonymousMode = false
        return true
    }

    override suspend fun removeUser(id: String) {
         getRealm().write {
            query(UserEntity::class, "id = $0", id)
                .first()
                .find()
                ?.let(::delete)
        }
    }

    override suspend fun getAllUsers(): List<SavedUserModel> {
        return getRealm().query(UserEntity::class)
            .find()
            .map(UserEntity::toSavedUserModel)
    }

    override fun switchAnonymousMode(enable: Boolean) {
        anonymousMode = enable
    }

    override suspend fun migrateDB(): Boolean {
        val savedCookies = getRealm().query(CookieEntity::class).find()
        val cookies = savedCookies.map(CookieEntity::toCookie)
        cookieStorage.addAll(cookies)
        return when(
            val checkResult = api.checkAuthorization()
        ) {
            is ApiResult.Error -> {
                // TODO: add message to log
                false
            }
            is ApiResult.Success -> {
                val user = checkResult.value
                val savedUser = saveUserToDB(user)
                if(savedUser != null) {
                    _currentUserModel.value = savedUser.toSavedUserModel()
                    _selectedUserID = savedUser.id
                    true
                } else {
                    false
                }
            }
        }
    }

    override fun initialize() {
        if(!anonymousMode) {
            val selectedUserId = _selectedUserID ?: return
            val savedUser = getRealm().query(
                UserEntity::class,
                "id = $0",
                selectedUserId
            )
            .first()
            .find()

            if(savedUser == null) return

            _currentUserModel.value = savedUser.toSavedUserModel()
            val cookies = savedUser.cookies.map(CookieEntity::toCookie)
            cookieStorage.addAll(cookies)
        }
    }

    private suspend fun saveUserToDB(user: UserModel): UserEntity? {
        val id = user.href.substringAfterLast('/')

        val savedUser = getRealm().query(UserEntity::class, "id = $0", id)
            .first()
            .find()
        if(savedUser != null) return savedUser

        val avatarFile = File(getFilesDir(), "/user_avatar/$id.png").also {
            it.parentFile?.mkdirs()
        }
        val successfulSaved = downloadImageToFile(
            url = user.avatarUrl,
            file = avatarFile,
            formatName = "png"
        )
        if(!successfulSaved) return null

        val userEntity = UserEntity(
            id = id,
            name = user.name,
            avatarPath = avatarFile.absolutePath,
            cookies = cookieStorage.getAll().mapTo(
                realmListOf(),
                Cookie::toEntity
            )
        )
        getRealm().write {
            copyToRealm(userEntity)
        }
        return userEntity
    }

    init {
        initialize()
    }
}