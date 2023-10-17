package ru.blays.preferences.DataStores


import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.blays.preferences.PreferenceModels.ReaderSettingsModel
import kotlin.reflect.KProperty

/*internal fun Context.createReaderPrefsStorage(): Storage<ReaderSettingsModel> = FileStorage(
    serializer = ReaderSettingsSerializer(),
    produceFile = {
        File(filesDir, "ReaderPrefs.json")
    }
)*/

class ReaderPrefsDSImpl(private val dataStore: DataStore<ReaderSettingsModel>): CoroutineScope {
    override val coroutineContext = Dispatchers.IO

    private val flow: Flow<ReaderSettingsModel> = dataStore.data

    var value: ReaderSettingsModel
        get() = runBlocking { flow.first() }
        set(value) {
            launch {
                dataStore.updateData {
                     value
                }
            }
        }

    @NonRestartableComposable
    @Composable
    fun asState(): State<ReaderSettingsModel> {
        return flow.collectAsState(value, context = coroutineContext)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun getValue(thisObj: Any?, property: KProperty<*>): ReaderSettingsModel = value

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun setValue(thisObj: Any?, property: KProperty<*>, value: ReaderSettingsModel) {
        this.value = value
    }
}


