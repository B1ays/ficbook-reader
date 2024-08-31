package ru.blays.ficbook.reader.shared.di

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import ru.blays.ficbook.reader.shared.data.realm.entity.*
import ru.blays.ficbook.reader.shared.data.realm.entity.blacklist.BlacklistAuthorEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.blacklist.BlacklistDirectionEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.blacklist.BlacklistFandomEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.blacklist.BlacklistTagEntity
import kotlin.reflect.KClass

internal val realmModule = module {
    single<Realm> {
        val configuration = RealmConfiguration.Builder(entities)
            .schemaVersion(3)
            .build()

        Realm.open(configuration)
    }
}

fun KoinComponent.injectRealm() = inject<Realm>()

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
        BlacklistAuthorEntity::class,
        BlacklistFandomEntity::class,
        BlacklistTagEntity::class,
        BlacklistDirectionEntity::class
    )
