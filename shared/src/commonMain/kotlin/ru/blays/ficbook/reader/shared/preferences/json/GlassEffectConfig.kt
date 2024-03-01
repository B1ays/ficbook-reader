package ru.blays.ficbook.reader.shared.preferences.json

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class GlassEffectConfig(
    val enabled: Boolean = true,
    val alpha: Float = 0.3F,
    val blurRadius: Float = 20F,
    val noiseFactor: Float = -1f,
) {
    companion object {
        val DEFAULT = GlassEffectConfig()
    }
}