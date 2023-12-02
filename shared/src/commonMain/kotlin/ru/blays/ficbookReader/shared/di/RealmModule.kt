package ru.blays.ficbookReader.shared.di

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin
import kotlin.reflect.KClass

internal val realmModule = module {
    factory<Realm> { params ->
        val configuration = RealmConfiguration.create(params.get())
        Realm.open(configuration)
    }
}

fun getRealm(vararg entities: KClass<out RealmObject>): Realm {
    return getKoin().get {
        parametersOf(entities.toSet())
    }
}

fun injectRealm(vararg entities: KClass<out RealmObject>): Lazy<Realm> {
    return getKoin().inject {
        parametersOf(entities.toSet())
    }
}
