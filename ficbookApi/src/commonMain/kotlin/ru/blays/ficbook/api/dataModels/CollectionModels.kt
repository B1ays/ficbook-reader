package ru.blays.ficbook.api.dataModels

/*@Serializable
data class CollectionCardModel(
    val relativeID: String,
    val realID: String,
    val name: String,
    val size: Int,
    val private: Boolean,
    val owner: UserModel
)*/

sealed class CollectionCardModel {
    data class Own(
        override val relativeID: String,
        override val realID: String,
        override val name: String,
        override val size: Int,
        val public: Boolean
    ): CollectionCardModel()

    data class Other(
        override val relativeID: String,
        override val realID: String,
        override val name: String,
        override val size: Int,
        val owner: UserModel,
        val subscribed: Boolean
    ): CollectionCardModel()

    abstract val relativeID: String
    abstract val realID: String
    abstract val name: String
    abstract val size: Int
}

sealed class CollectionPageModel {
    data class Own(
        override val name: String,
        override val description: String? = null,
        override val filterParams: CollectionFilterParams
    ): CollectionPageModel()

    data class Other(
        override val name: String,
        override val description: String?,
        val owner: UserModel,
        val subscribed: Boolean,
        override val filterParams: CollectionFilterParams
    ): CollectionPageModel()
    abstract val name: String
    abstract val description: String?
    abstract val filterParams: CollectionFilterParams
}

data class CollectionFilterParams(
    val availableSortParams: List<Pair<String, String>>,
    val availableDirections: List<Pair<String, String>>,
    val availableFandoms: List<Pair<String, String>>
)

data class CollectionMainInfoModel(
    val name: String,
    val description: String,
    val public: Boolean
)