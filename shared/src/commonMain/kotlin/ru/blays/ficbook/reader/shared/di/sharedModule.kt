package ru.blays.ficbook.reader.shared.di

import org.koin.dsl.module
import ru.blays.ficbook.api.di.apiModule

val sharedModule = module {
    includes(
        okHttpModule,
        apiModule,
        repositoryModule,
        realmModule
    )
}