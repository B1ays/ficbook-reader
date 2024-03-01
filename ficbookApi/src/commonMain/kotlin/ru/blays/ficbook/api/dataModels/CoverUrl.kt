package ru.blays.ficbook.api.dataModels

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class CoverUrl(val url: String)