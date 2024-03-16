package ru.blays.ficbook.reader.shared.di

import org.koin.dsl.module
import ru.blays.ficbook.api.di.ficbookApiModule

val sharedModule = module {
    includes(
        okHttpModule,
        ficbookApiModule,
        repositoryModule,
        realmModule
    )
}