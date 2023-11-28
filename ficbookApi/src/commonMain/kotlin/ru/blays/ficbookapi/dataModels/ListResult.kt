package ru.blays.ficbookapi.dataModels

data class ListResult <T: Any> (
    val list: List<T>,
    val hasNextPage: Boolean
)
