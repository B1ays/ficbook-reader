package ru.blays.ficbookReader.shared.data.di

import org.koin.dsl.module
import ru.blays.ficbookapi.ficbookConnection.FicbookApi
import ru.blays.ficbookapi.ficbookConnection.IFicbookApi

val ficbookApiModule = module(createdAtStart = true) {
    single<IFicbookApi> {
        println("Create FicbookApi")
        FicbookApi()
    }
}