package ru.blays.ficbookReader.shared.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class SectionWithQuery(
    val name: String,
    val path: String,
    val queryParameters: List<Pair<String, String>>
)

@Serializable
data class Section(
    val name: String,
    val segments: String,
)

fun Section.toSectionWithQuery() = SectionWithQuery(
    name = name,
    path = segments,
    queryParameters = emptyList()
)

