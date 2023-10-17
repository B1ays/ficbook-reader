package ru.blays.ficbookReader.shared.data.di

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import org.koin.dsl.module
import ru.blays.ficbookReader.shared.data.realmModels.CookieEntity
import kotlin.reflect.KClass

val realmModule = module {
    factory<Realm> {
        val schema: Set<KClass<out RealmObject>> = setOf(CookieEntity::class)
        val configuration = RealmConfiguration.create(schema)
        Realm.open(configuration)
    }
}

