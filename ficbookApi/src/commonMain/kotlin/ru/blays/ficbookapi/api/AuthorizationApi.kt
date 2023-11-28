package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbookapi.LOGIN_CHECK
import ru.blays.ficbookapi.SETTING_HREF
import ru.blays.ficbookapi.dataModels.AuthorizationResponseModel
import ru.blays.ficbookapi.dataModels.AuthorizationResult
import ru.blays.ficbookapi.dataModels.LoginModel
import ru.blays.ficbookapi.dataModels.UserModel
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.okHttpDsl.formBody
import ru.blays.ficbookapi.okHttpDsl.get
import ru.blays.ficbookapi.okHttpDsl.post
import ru.blays.ficbookapi.okHttpDsl.stringOrThrow
import ru.blays.ficbookapi.parsers.UserParser
import ru.blays.ficbookapi.result.ApiResult

interface AuthorizationApi {
    suspend fun logIn(loginModel: LoginModel): ApiResult<AuthorizationResult>
    suspend fun checkAuthorization(): ApiResult<UserModel>
}

class AuthorizationApiImpl(
    private val client: OkHttpClient
): AuthorizationApi {
    private val userParser = UserParser()

    override suspend fun logIn(loginModel: LoginModel): ApiResult<AuthorizationResult> = coroutineScope {
        return@coroutineScope try {
            val response = client.post(
                body = formBody {
                    add("login", loginModel.login)
                    add("password", loginModel.password)
                    add("remember", loginModel.remember.toString())
                }
            ) {
                ficbookUrl {
                    addPathSegment(LOGIN_CHECK)
                }
                addHeader("authority", "ficbook.net")
                addHeader("origin", "https://ficbook.net")
                addHeader("referer", "https://ficbook.net/")
            }
            val body = response.body.stringOrThrow()
            val resultModel: AuthorizationResponseModel = Json.decodeFromString(body)
            val user = checkAuthorization()
            ApiResult.success(
                AuthorizationResult(
                    responseResult = resultModel,
                    user = user.getOrNull()
                )
            )
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }

    override suspend fun checkAuthorization(): ApiResult<UserModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                ficbookUrl {
                    addPathSegment(SETTING_HREF)
                }
            }
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val user = userParser.parse(document)
            ApiResult.success(user)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
}