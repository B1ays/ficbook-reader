package ru.blays.ficbook.reader.shared.data.realm.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import ru.blays.ficbook.api.dataModels.CookieModel

@Suppress("unused", "PropertyName")
class CookieEntity(): RealmObject {
    @PrimaryKey
    var _id: ObjectId = ObjectId()
    var name: String = ""
    var value: String = ""
    var domain: String? = null
    var path: String? = null
    var secure: Boolean = false

    constructor(
        name: String = "",
        value: String = "",
        domain: String? = null,
        path: String? = null,
        secure: Boolean = false
    ): this() {
        this.name = name
        this.value = value
        this.domain = domain
        this.path = path
        this.secure = secure
    }
}

fun CookieEntity.toApiModel(): CookieModel = CookieModel(
    name = name,
    value = value
)

fun CookieModel.toEntity() = CookieEntity(
    name = name,
    value = value
)