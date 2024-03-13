package ru.blays.ficbook.reader.shared.components.authorProfile.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform.getKoin
import ru.blays.ficbook.api.result.ApiResult
import ru.blays.ficbook.reader.shared.data.repo.declaration.IAuthorProfileRepo

class DefaultAuthorFollowComponent(
    componentContext: ComponentContext,
    initialValue: Boolean = false,
    private var authorID: String? = null,
): ComponentContext by componentContext {
    private val repo: IAuthorProfileRepo by getKoin().inject()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _state = MutableValue(initialValue = initialValue)

    val state: Value<Boolean> get() = _state

    fun changeFollow() {
        val newValue = !state.value
        coroutineScope.launch {
            authorID?.let {
                when (
                    val result = repo.changeFollow(
                        follow = newValue,
                        id = it
                    )
                ) {
                    is ApiResult.Error -> {}
                    is ApiResult.Success -> {
                        if(result.value) {
                            _state.value = newValue
                        }
                    }
                }
            }
        }
    }

    internal fun update(authorID: String, currentValue: Boolean) {
        this.authorID = authorID
        _state.value = currentValue
    }
}