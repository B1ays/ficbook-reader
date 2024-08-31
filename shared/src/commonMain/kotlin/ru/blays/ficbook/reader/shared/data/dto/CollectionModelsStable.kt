package ru.blays.ficbook.reader.shared.data.dto

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
sealed class CollectionCardModelStable {
    @Stable
    data class Own(
        override val relativeID: String,
        override val realID: String,
        override val name: String,
        override val size: Int,
        val public: Boolean
    ): CollectionCardModelStable()

    @Stable
    data class Other(
        override val relativeID: String,
        override val realID: String,
        override val name: String,
        override val size: Int,
        val owner: UserModelStable,
        val subscribed: Boolean
    ): CollectionCardModelStable()

    abstract val relativeID: String
    abstract val realID: String
    abstract val name: String
    abstract val size: Int
}

@Serializable
@Stable
sealed class CollectionPageModelStable {
    @Serializable
    @Stable
    data class Own(
        override val name: String,
        override val description: String? = null,
        override val filterParams: CollectionFilterParamsStable
    ): CollectionPageModelStable()

    @Serializable
    @Stable
    data class Other(
        override val name: String,
        override val description: String?,
        val owner: UserModelStable,
        val subscribed: Boolean,
        override val filterParams: CollectionFilterParamsStable
    ): CollectionPageModelStable()

    fun copyPage(
        name: String = this.name,
        description: String? = this.description,
    ): CollectionPageModelStable = when(this) {
        is Own -> this.copy(
            name = name,
            description = description
        )
        is Other -> this.copy(
            name = name,
            description = description
        )
    }

    abstract val name: String
    abstract val description: String?
    abstract val filterParams: CollectionFilterParamsStable
}

@Serializable
@Immutable
data class CollectionFilterParamsStable(
    val availableSortParams: List<Pair<String, String>>,
    val availableDirections: List<Pair<String, String>>,
    val availableFandoms: List<Pair<String, String>>
)