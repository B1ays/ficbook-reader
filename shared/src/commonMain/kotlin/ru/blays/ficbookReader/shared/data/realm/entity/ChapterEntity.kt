package ru.blays.ficbookReader.shared.data.realm.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

@Suppress("PropertyName")
class ChapterEntity(
    @PrimaryKey var _id: ObjectId = ObjectId(),
    var fanficID: String,
    var name: String,
    var text: String,
    var href: String?,
    var lastWatchedCharIndex: Int,
    var readed: Boolean
): RealmObject {
    constructor(): this(
        name = "Глава 1",
        fanficID = "",
        text = "",
        href = null,
        lastWatchedCharIndex = 0,
        readed = false
    )
}

/*
data object ChapterEntityMigration : AutomaticSchemaMigration {
    override fun migrate(migrationContext: AutomaticSchemaMigration.MigrationContext) {
        if(migrationContext.oldRealm.schemaVersion() == 0L) {
            val oldRealm = migrationContext.oldRealm
            val newRealm = migrationContext.newRealm
            val oldChapters = oldRealm.where<ChapterEntity>().findAll()
            val newChapters = newRealm.where<ChapterEntity>().findAll()
    }
}*/
