package ru.blays.ficbookReader.shared.di

import org.koin.dsl.module
import ru.blays.ficbookapi.di.apiModule

val sharedModule = module {
    includes(
        okHttpModule,
        apiModule,
        repositoryModule,
        realmModule
    )
}