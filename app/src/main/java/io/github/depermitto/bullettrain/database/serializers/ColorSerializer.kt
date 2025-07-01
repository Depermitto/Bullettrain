package io.github.depermitto.bullettrain.database.serializers

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("androidx.compose.ui.graphics.Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeSerializableValue(ULong.serializer(), value.value)
    override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeSerializableValue(ULong.serializer()))
}