package ru.blays.ficbookReader.shared.ui.searchComponents.declaration

import com.arkivanov.decompose.value.Value
import ru.blays.ficbookReader.shared.data.dto.SearchedCharacterModel
import ru.blays.ficbookReader.shared.data.dto.SearchedCharactersGroup
import ru.blays.ficbookReader.shared.data.dto.SearchedPairingModel

interface SearchPairingsComponent {
    val state: Value<State>

    val defaultCharacterModifiers: Array<String>

    fun selectPairing(
        select: Boolean,
        pairing: SearchedPairingModel
    )
    fun excludePairing(
        exclude: Boolean,
        pairing: SearchedPairingModel
    )

    fun addCharacterToPairing(character: SearchedCharacterModel)

    fun clearBuildedPairing()

    fun changeCharacterModifier(character: SearchedPairingModel.Character, modifier: String)

    data class State(
        val searchedCharacters: List<SearchedCharactersGroup>,
        val buildedPairing: SearchedPairingModel?,
        val selectedPairings: Set<SearchedPairingModel>,
        val excludedPairings: Set<SearchedPairingModel>,
        val loading: Boolean,
        val error: Boolean,
        val errorMessage: String?
    )
}

interface InternalSearchPairingsComponent: SearchPairingsComponent {
    fun update(fandomIDs: List<String>)

    fun excludeNotLinkedPairings(fandomIds: List<String>)

    fun clean()
}