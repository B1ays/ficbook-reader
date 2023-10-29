package ru.blays.ficbookReader.shared.di

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.koin.dsl.module

internal val realmModule = module {
    factory<Realm> { params ->
        val configuration = RealmConfiguration.create(params.get())
        Realm.open(configuration)
    }
}