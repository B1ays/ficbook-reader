package ru.blays.ficbook.reader.shared.data.realm.entity.blacklist

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class BlacklistTagEntity(
    @PrimaryKey var _id: ObjectId = ObjectId(),
    var tagName: String
): RealmObject {
    constructor(): this(
        _id = ObjectId(),
        tagName = ""
    )
}