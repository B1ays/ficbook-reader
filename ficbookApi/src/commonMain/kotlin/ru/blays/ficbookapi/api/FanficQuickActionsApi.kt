package ru.blays.ficbookapi.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import ru.blays.ficbookapi.dataModels.FanficQuickActionsModel
import ru.blays.ficbookapi.ficbookExtensions.ficbookUrl
import ru.blays.ficbookapi.json
import ru.blays.ficbookapi.okHttpDsl.formBody
import ru.blays.ficbookapi.okHttpDsl.href
import ru.blays.ficbookapi.okHttpDsl.post
import ru.blays.ficbookapi.okHttpDsl.stringOrThrow
import ru.blays.ficbookapi.result.ApiResult

interface FanficQuickActionsApi {
    suspend fun get(fanficID: String): ApiResult<FanficQuickActionsModel>
}

class FanficQuickActionsApiImpl(
    val client: OkHttpClient
): FanficQuickActionsApi {
    override suspend fun get(fanficID: String): ApiResult<FanficQuickActionsModel> = coroutineScope {
        try {
            val response = client.post(
                body = formBody {
                    add("fanfic_id", fanficID)
                }
            ) {
                ficbookUrl {
                    href("ajax/fanfic_actions_state")
                }
            }
            val body = response.body.stringOrThrow()
            val model: FanficQuickActionsModel = json.decodeFromString(body)
            ApiResult.success(model)
        } catch (e: Exception) {
            ApiResult.failure(e)
        }
    }
}