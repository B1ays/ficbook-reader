package ru.blays.ficbook.api.dataModels

data class ListResult <T: Any> (
    val list: List<T>,
    val hasNextPage: Boolean
)