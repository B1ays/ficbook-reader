package ru.blays.ficbook.reader.shared.data.realm.entity.blacklist

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class BlacklistAuthorEntity(
    @PrimaryKey var _id: ObjectId = ObjectId(),
    var authorName: String
): RealmObject {
    constructor(): this(
        _id = ObjectId(),
        authorName = "",
    )
}