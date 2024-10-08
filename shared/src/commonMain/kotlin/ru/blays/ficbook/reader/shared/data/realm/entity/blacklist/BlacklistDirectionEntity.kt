package ru.blays.ficbook.reader.shared.data.realm.entity.blacklist

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId
import ru.blays.ficbook.reader.shared.data.dto.FanficDirection

class BlacklistDirectionEntity(
    @PrimaryKey var _id: ObjectId = ObjectId(),
    var direction: String = FanficDirection.UNKNOWN.name
): RealmObject {
    val directionEnum: FanficDirection
        get() = FanficDirection.getForName(direction)

    constructor(direction: String): this() {
        this.direction = direction
    }

    constructor(): this(
        _id = ObjectId(),
        direction = FanficDirection.UNKNOWN.name
    )
}