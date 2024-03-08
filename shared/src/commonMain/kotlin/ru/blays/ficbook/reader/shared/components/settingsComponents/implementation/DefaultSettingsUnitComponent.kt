package ru.blays.ficbook.reader.shared.components.settingsComponents.implementation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject
import ru.blays.ficbook.reader.shared.preferences.repositiry.ISettingsRepository
import ru.blays.ficbook.reader.shared.components.settingsComponents.declaration.SettingsUnitComponent

class DefaultSettingsUnitComponent<T: Any>(
    componentContext: ComponentContext,
    key: ISettingsRepository.SettingsKey<T>,
    defaultValue: T
): SettingsUnitComponent<T>, ComponentContext by componentContext {
    val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val settingsRepository: ISettingsRepository by inject(ISettingsRepository::class.java)
    private var settingsDelegate by settingsRepository.getDelegate(
        key = key,
        defaultValue = defaultValue
    )
    private val _state: Flow<T> by settingsRepository.getFlowDelegate(
        key = key,
        defaultValue = settingsDelegate
    )
    override val state: StateFlow<T> = runBlocking { _state.stateIn(coroutineScope) }

    override fun onIntent(intent: SettingsUnitComponent.Intent<T>) {
        when(intent) {
            is SettingsUnitComponent.Intent.ChangeValue<T> -> {
                settingsDelegate = intent.value
            }
        }
    }

    init {
        lifecycle.doOnDestroy {
            coroutineScope.cancel()
        }
    }
}