package ru.blays.ficbookReader.shared.di

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.realm.entity.ChapterEntity
import ru.blays.ficbookReader.shared.data.realm.entity.CookieEntity

internal val realmModule = module {
    single<Realm> {
        val schema = setOf(ChapterEntity::class, CookieEntity::class)
        val configuration = RealmConfiguration.Builder(schema)
            .schemaVersion(2)
            .deleteRealmIfMigrationNeeded()

        Realm.open(configuration.build())
    }
}

fun getRealm(): Realm {
    return getKoin().get()
}

fun injectRealm(): Lazy<Realm> {
    return getKoin().inject()
}
