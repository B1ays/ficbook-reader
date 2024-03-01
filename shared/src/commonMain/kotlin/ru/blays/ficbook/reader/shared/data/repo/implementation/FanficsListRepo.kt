package ru.blays.ficbook.reader.shared.data.repo.implementation

import ru.blays.ficbook.reader.shared.data.dto.FanficCardModelStable
import ru.blays.ficbook.reader.shared.data.dto.SectionWithQuery
import ru.blays.ficbook.reader.shared.data.mappers.toApiModel
import ru.blays.ficbook.reader.shared.data.mappers.toStableModel
import ru.blays.ficbook.reader.shared.data.repo.declaration.IFanficsListRepo
import ru.blays.ficbook.api.api.FanficsListApi
import ru.blays.ficbook.api.dataModels.FanficCardModel
import ru.blays.ficbook.api.dataModels.ListResult
import ru.blays.ficbook.api.result.ApiResult

class FanficsListRepo(
    private val api: FanficsListApi
): IFanficsListRepo {
    override suspend fun get(href: String, page: Int): ApiResult<ListResult<FanficCardModelStable>> {
        return when(
            val result = api.get(href, page)
        ) {
            is ApiResult.Error -> {
                ApiResult.failure(result.exception)
            }
            is ApiResult.Success -> {
                ApiResult.success(
                    ListResult(
                        list = result.value.list.map(FanficCardModel::toStableModel),
                        hasNextPage = result.value.hasNextPage
                    )
                )
            }
        }
    }

    override suspend fun get(section: SectionWithQuery, page: Int): ApiResult<ListResult<FanficCardModelStable>> {
        return when(
            val result = api.get(
                section = section.toApiModel(),
                page = page
            )
        ) {
            is ApiResult.Error -> {
                ApiResult.failure(result.exception)
            }
            is ApiResult.Success -> {
                ApiResult.success(
                    ListResult(
                        list = result.value.list.map(FanficCardModel::toStableModel),
                        hasNextPage = result.value.hasNextPage
                    )
                )
            }
        }
    }
}