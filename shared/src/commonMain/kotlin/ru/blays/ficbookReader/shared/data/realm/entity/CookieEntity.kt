package ru.blays.ficbookReader.shared.data.realm.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import ru.blays.ficbookapi.dataModels.CookieModel

@Suppress("unused", "PropertyName")
class CookieEntity(): RealmObject {
    constructor(
        name: String,
        value: String
    ): this() {
        this.name = name
        this.value = value
    }

    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = ""
    var value: String = ""
}

fun CookieEntity.toApiModel(): CookieModel = CookieModel(
    name = name,
    value = value
)

fun CookieModel.toEntity() = CookieEntity(
    name = name,
    value = value
)