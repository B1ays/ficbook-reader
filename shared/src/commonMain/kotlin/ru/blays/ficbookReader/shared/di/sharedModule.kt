package ru.blays.ficbookReader.shared.di

import org.koin.dsl.module

val sharedModule = module {
    includes(
        ficbookApiModule,
        realmModule,
        repositoryModule
    )
}