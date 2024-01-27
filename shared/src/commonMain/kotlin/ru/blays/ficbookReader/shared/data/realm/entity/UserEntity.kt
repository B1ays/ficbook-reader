package ru.blays.ficbookReader.shared.data.realm.entity

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

@Suppress("PropertyName")
class UserEntity(
    @PrimaryKey var _id: ObjectId = ObjectId(),
    var name: String,
    var id: String,
    var avatarPath: String,
    var cookies: RealmList<CookieEntity>
): RealmObject {
    constructor() : this(
        name = "",
        id = "",
        avatarPath = "",
        cookies = realmListOf()
    )
}