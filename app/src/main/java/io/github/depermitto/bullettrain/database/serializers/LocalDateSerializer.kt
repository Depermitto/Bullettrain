package io.github.depermitto.bullettrain.database.serializers

import java.time.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalDateSerializer : KSerializer<LocalDate> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("java.time.LocalDate", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LocalDate) =
    encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}
