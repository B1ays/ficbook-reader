package ru.blays.ficbook.reader.shared.components.collectionComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbook.reader.shared.components.collectionComponents.implementation.EditCollectionDialogConfig
import ru.blays.ficbook.reader.shared.data.dto.CollectionCardModelStable
import ru.blays.ficbook.reader.shared.data.dto.UserModelStable

interface CollectionsListComponent {
    val state: Value<State>

    fun sendIntent(intent: Intent)

    fun onOutput(output: Output)

    sealed class Intent {
        data object Refresh: Intent()
        data class CreateCollection(
            val name: String,
            val description: String,
            val public: Boolean
        ): Intent()
        data class UpdateCollection(
            val relativeID: String,
            val realID: String
        ): Intent() {
            constructor(collection: CollectionCardModelStable) : this(
                relativeID = collection.relativeID,
                realID = collection.realID
            )
        }
        data class DeleteCollection(val realID: String): Intent()
        data class ChangeSubscription(val collection: CollectionCardModelStable.Other) : Intent()
    }

    sealed class Output {
        data class OpenCollection(
            val relativeID: String,
            val realID: String,
            val initialDialogConfig: EditCollectionDialogConfig?
        ): Output() {
            constructor(collection: CollectionCardModelStable) : this(
                relativeID = collection.relativeID,
                realID = collection.realID,
                initialDialogConfig = null
            )
            constructor(
                collection: CollectionCardModelStable,
                initialDialogConfig: EditCollectionDialogConfig
            ) : this(
                relativeID = collection.relativeID,
                realID = collection.realID,
                initialDialogConfig = initialDialogConfig
            )
        }
        data class OpenUser(val owner: UserModelStable): Output()
    }

    data class State(
        val list: List<CollectionCardModelStable> = emptyList(),
        val isLoading: Boolean = false,
        val isError: Boolean = false
    )
}

internal interface CollectionsListComponentInternal: CollectionsListComponent {
    fun refresh()
}