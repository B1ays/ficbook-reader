package ru.blays.ficbookReader.shared.di

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.realm.entity.*
import kotlin.reflect.KClass

internal val realmModule = module {
    single<Realm> {
        val configuration = RealmConfiguration.Builder(entities)
            .schemaVersion(2)
            .build()

        Realm.open(configuration)
    }
}

fun getRealm(): Realm {
    return getKoin().get()
}

fun injectRealm(): Lazy<Realm> {
    return getKoin().inject()
}

private val entities: Set<KClass<out RealmObject>>
    get() = setOf(
        ChapterEntity::class,
        CookieEntity::class,
        UserEntity::class,
        SearchParamsEntity::class,
        FandomEntity::class,
        PairingEntity::class,
        PairingEntity.CharacterEntity::class,
        TagEntity::class,
        IntRangeEntity::class,
        LongRangeEntity::class,
    )
