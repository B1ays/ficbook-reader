package ru.blays.ficbookReader.shared.data.mappers

import ru.blays.ficbookReader.shared.data.dto.CookieModelStable
import ru.blays.ficbookReader.shared.data.dto.LoginModelStable
import ru.blays.ficbookReader.shared.data.dto.SectionWithQuery
import ru.blays.ficbookapi.dataModels.CookieModel
import ru.blays.ficbookapi.dataModels.LoginModel

fun LoginModelStable.toApiModel() = LoginModel(
    login = login,
    password = password,
    remember = remember
)

fun SectionWithQuery.toApiModel() = ru.blays.ficbookapi.data.SectionWithQuery(
    name = name,
    path = path,
    queryParameters = queryParameters
)

fun CookieModelStable.toApiModel() = CookieModel(
    name = name,
    value = value
)