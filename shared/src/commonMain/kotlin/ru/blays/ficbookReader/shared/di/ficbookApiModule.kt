package ru.blays.ficbookReader.shared.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.blays.ficbookapi.ficbookConnection.FicbookApi
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

internal val ficbookApiModule = module {
    singleOf(::FicbookApi) bind IFicbookApi::class
}