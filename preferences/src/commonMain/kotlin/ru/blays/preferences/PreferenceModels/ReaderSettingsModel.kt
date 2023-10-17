package ru.blays.preferences.PreferenceModels


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

import java.io.InputStream
import java.io.OutputStream

@Serializable
data class ReaderSettingsModel(
    val fontSize: Int,
    val colorDark: Int,
    val colorLight: Int,
    val fullscreenMode: Boolean,
    val nightMode: Boolean
)

@Suppress("BlockingMethodInNonBlockingContext")
@OptIn(ExperimentalSerializationApi::class)
internal class ReaderSettingsSerializer: Serializer<ReaderSettingsModel> {
    override val defaultValue: ReaderSettingsModel = ReaderSettingsModel(
        fontSize = 16,
        colorDark = Color.Black.toArgb(),
        colorLight = Color.White.toArgb(),
        fullscreenMode = true,
        nightMode = true
    )

    override suspend fun readFrom(input: InputStream): ReaderSettingsModel {
        return try {
            Json.decodeFromStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        } finally {
            input.close()
        }
    }

    override suspend fun writeTo(t: ReaderSettingsModel, output: OutputStream) {
        Json.encodeToStream(t, output)
        output.close()
    }

}