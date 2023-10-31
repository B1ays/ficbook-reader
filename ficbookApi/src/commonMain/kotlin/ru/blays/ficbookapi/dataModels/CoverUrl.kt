package ru.blays.ficbookapi.dataModels

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class CoverUrl(val url: String)