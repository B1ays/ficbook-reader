package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import ru.blays.ficbook.api.FICBOOK_LOGIN_LINK
import ru.blays.ficbook.api.LOGIN_CHECK
import ru.blays.ficbook.api.SETTING_HREF
import ru.blays.ficbook.api.dataModels.AuthorizationResponseModel
import ru.blays.ficbook.api.dataModels.AuthorizationResult
import ru.blays.ficbook.api.dataModels.LoginModel
import ru.blays.ficbook.api.dataModels.UserModel
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.json
import ru.blays.ficbook.api.okHttpDsl.*
import ru.blays.ficbook.api.parsers.UserParser
import ru.blays.ficbook.api.result.ApiResult


interface AuthorizationApi {
    suspend fun logIn(loginModel: LoginModel): ApiResult<AuthorizationResult>
    suspend fun checkAuthorization(): ApiResult<UserModel>
}

internal class AuthorizationApiImpl(
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
            val resultModel: AuthorizationResponseModel = json.decodeFromString(body)
            val user = checkAuthorization().getOrThrow()
            ApiResult.success(
                AuthorizationResult(
                    responseResult = resultModel,
                    user = user
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResult.failure(e)
        }
    }

    override suspend fun checkAuthorization(): ApiResult<UserModel> = coroutineScope {
        return@coroutineScope try {
            val response = client.get {
                ficbookUrl {
                    href(SETTING_HREF)
                }
            }
            val finalUrl = response.request.url.toString()
            if(finalUrl == FICBOOK_LOGIN_LINK) throw(
                Exception("User not authorized")
            )
            val body: String = response.body.stringOrThrow()
            val document = Jsoup.parse(body)
            val user = userParser.parse(document)
            ApiResult.success(user)
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResult.failure(e)
        }
    }
}