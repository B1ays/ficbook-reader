package ru.blays.ficbookapi.dataModels

data class FanficsListResult(
    val fanfics: List<FanficCardModel>,
    val hasNextPage: Boolean
)
