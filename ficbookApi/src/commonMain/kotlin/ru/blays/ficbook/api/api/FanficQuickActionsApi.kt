package ru.blays.ficbook.api.api

import kotlinx.coroutines.coroutineScope
import okhttp3.OkHttpClient
import ru.blays.ficbook.api.dataModels.FanficQuickActionsModel
import ru.blays.ficbook.api.ficbookExtensions.ficbookUrl
import ru.blays.ficbook.api.json
import ru.blays.ficbook.api.okHttpDsl.formBody
import ru.blays.ficbook.api.okHttpDsl.href
import ru.blays.ficbook.api.okHttpDsl.post
import ru.blays.ficbook.api.okHttpDsl.stringOrThrow
import ru.blays.ficbook.api.result.ApiResult

interface FanficQuickActionsApi {
    suspend fun get(fanficID: String): ApiResult<FanficQuickActionsModel>
}

internal class FanficQuickActionsApiImpl(
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