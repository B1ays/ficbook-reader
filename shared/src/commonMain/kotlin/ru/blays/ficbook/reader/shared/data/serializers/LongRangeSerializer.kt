package ru.blays.ficbook.reader.shared.data.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LongRangeSerializer: KSerializer<LongRange> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            "ru.blays.ficbook.reader.shared.data.serializers.LongRangeSerializer",
            PrimitiveKind.STRING
        )

    override fun deserialize(decoder: Decoder): LongRange {
        val string = decoder.decodeString()
        val start = string.substringBefore('-').toLong()
        val end = string.substringAfter('-').toLong()
        return start..end
    }

    override fun serialize(encoder: Encoder, value: LongRange) {
        return encoder.encodeString("${value.first}-${value.last}")
    }
}