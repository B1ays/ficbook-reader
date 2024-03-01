package ru.blays.ficbook.api.dataModels

import kotlinx.serialization.Serializable

@Serializable
data class PairingModel(
    val character: String,
    val href: String,
    val isHighlighted: Boolean
)
