package ru.blays.ficbookReader.shared.data.realm.utils

import io.realm.kotlin.MutableRealm
import io.realm.kotlin.UpdatePolicy
import ru.blays.ficbookReader.shared.data.realm.entity.CookieEntity

/**
 * Copy entities List to realm
 *
 * Same as default copyToRealm function, but copy all entities
  * */
fun MutableRealm.copyToRealm(
    entities: List<CookieEntity>,
    updatePolicy: UpdatePolicy = UpdatePolicy.ERROR
) {
    for (entity in entities) {
        copyToRealm(entity, updatePolicy)
    }
}