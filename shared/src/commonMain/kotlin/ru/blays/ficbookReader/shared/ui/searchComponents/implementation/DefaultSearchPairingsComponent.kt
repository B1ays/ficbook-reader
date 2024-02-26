package ru.blays.ficbookReader.shared.ui.searchComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbookReader.shared.data.dto.SearchedCharacterModel
import ru.blays.ficbookReader.shared.data.dto.SearchedPairingModel
import ru.blays.ficbookReader.shared.data.repo.declaration.ISearchRepo
import ru.blays.ficbookReader.shared.ui.Utils.ExternalStateUpdatable
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.InternalSearchPairingsComponent
import ru.blays.ficbookReader.shared.ui.searchComponents.declaration.SearchPairingsComponent
import ru.blays.ficbookapi.result.ApiResult

class DefaultSearchPairingsComponent(
    componentContext: ComponentContext,
): InternalSearchPairingsComponent,
    ExternalStateUpdatable<SearchPairingsComponent.State>,
    ComponentContext by componentContext {
    private val repository: ISearchRepo by getKoin().inject()

    private val _state = MutableValue(
        SearchPairingsComponent.State(
            searchedCharacters = emptyList(),
            buildedPairing = null,
            selectedPairings = setOf(),
            excludedPairings = setOf(),
            loading = false,
            error = false,
            errorMessage = null
        )
    )

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val state get() = _state

    override val defaultCharacterModifiers: Array<String> = getDefaultModifiers()

    override fun selectPairing(select: Boolean, pairing: SearchedPairingModel) {
        if(select) {
            _state.update {
                it.copy(
                    selectedPairings = it.selectedPairings + pairing,
                    buildedPairing = null
                )
            }
        } else {
            _state.update {
                it.copy(
                    selectedPairings = it.selectedPairings - pairing
                )
            }
        }
    }

    override fun excludePairing(exclude: Boolean, pairing: SearchedPairingModel) {
        if(exclude) {
            _state.update {
                it.copy(
                    excludedPairings = it.excludedPairings + pairing,
                    buildedPairing = null
                )
            }
        } else {
            _state.update {
                it.copy(
                    excludedPairings = it.excludedPairings - pairing
                )
            }
        }
    }

    override fun addCharacterToPairing(character: SearchedCharacterModel) {
        _state.update {
            var buildedPairing = it.buildedPairing
            if(buildedPairing == null) {
                buildedPairing = SearchedPairingModel(
                    setOf(
                        SearchedPairingModel.Character(
                            fandomId = character.fandomId,
                            id = character.id,
                            name = character.name
                        )
                    )
                )
            } else {
                buildedPairing = buildedPairing.copy(
                    characters = buildedPairing.characters + SearchedPairingModel.Character(
                        fandomId = character.fandomId,
                        id = character.id,
                        name = character.name
                    )
                )
            }
            it.copy(buildedPairing = buildedPairing)
        }
    }

    override fun clearBuildedPairing() {
        _state.update {
            it.copy(buildedPairing = null)
        }
    }

    override fun changeCharacterModifier(character: SearchedPairingModel.Character, modifier: String) {
        if(character.modifier == modifier) return
        _state.update {
            val newCharacter = character.copy(
                modifier = modifier
            )
            val index = it.buildedPairing?.characters?.indexOf(character) ?: return@update it
            if(index == -1) return@update it
            val characters = it.buildedPairing.characters.toMutableList().apply {
                this[index] = newCharacter
            }
            it.copy(
                buildedPairing = it.buildedPairing.copy(
                    characters = characters.toSet()
                )
            )
        }
    }

    override fun update(fandomIDs: List<String>) {
        coroutineScope.launch {
            _state.update {
                it.copy(loading = true)
            }
            when(
                val result = repository.getCharacters(fandomIDs)
            ) {
                is ApiResult.Error -> {
                    _state.update {
                        result.exception.printStackTrace()
                        it.copy(
                            loading = false,
                            error = true,
                            errorMessage = result.exception.message
                        )
                    }
                }
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(searchedCharacters = result.value)
                    }
                }
            }
        }
    }

    override fun clean() {
        _state.update {
            it.copy(
                searchedCharacters = emptyList(),
                buildedPairing = null,
                selectedPairings = emptySet(),
                excludedPairings = emptySet(),
                loading = false,
                error = false,
                errorMessage = null
            )
        }
    }

    override fun excludeNotLinkedPairings(fandomIds: List<String>) {
        _state.update {
            val filteredSelectedPairings = it.selectedPairings.filterTo(mutableSetOf()) {
                it.characters.all { it.fandomId in fandomIds }
            }
            val filteredExcludedPairings = it.excludedPairings.filterTo(mutableSetOf()) {
                it.characters.all { it.fandomId in fandomIds }
            }

            val filteredBuildedPairing = if(it.buildedPairing?.characters?.all { it.fandomId in fandomIds } == true) {
                it.buildedPairing
            } else {
                null
            }

            it.copy(
                selectedPairings = filteredSelectedPairings,
                excludedPairings = filteredExcludedPairings,
                buildedPairing = filteredBuildedPairing
            )
        }
    }

    override fun updateState(block: (SearchPairingsComponent.State) -> SearchPairingsComponent.State) {
        _state.update(block)
    }

    private fun getDefaultModifiers(): Array<String> = arrayOf(
        "fem",
        "male",
        "dark",
        "light",
        "alt",
        "reverse",
        "de-aged",
        "kid",
        "adult",
        "альфа",
        "омега",
        "бета"
    )
}