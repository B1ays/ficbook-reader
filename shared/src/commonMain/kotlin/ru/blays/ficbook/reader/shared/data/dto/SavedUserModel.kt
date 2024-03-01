package ru.blays.ficbook.reader.shared.data.dto

import io.realm.kotlin.ext.toRealmList
import ru.blays.ficbook.reader.shared.data.realm.entity.CookieEntity
import ru.blays.ficbook.reader.shared.data.realm.entity.UserEntity

data class SavedUserModel(
    val name: String,
    val id: String,
    val avatarPath: String,
    val cookies: List<CookieEntity>
)

fun SavedUserModel.toUserEntity() = UserEntity(
    name = name,
    id = id,
    avatarPath = avatarPath,
    cookies = cookies.toRealmList()
)

fun UserEntity.toSavedUserModel() = SavedUserModel(
    name = name,
    id = id,
    avatarPath = avatarPath,
    cookies = cookies
)
