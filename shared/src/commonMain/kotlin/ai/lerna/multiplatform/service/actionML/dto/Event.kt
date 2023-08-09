package ai.lerna.multiplatform.service.actionML.dto

import com.soywiz.klock.DateFormat
import com.soywiz.klock.DateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object DateTimeSerializer : KSerializer<DateTime> {
	override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): DateTime {
		return DateTime.fromString(decoder.decodeString()).utc
	}

	override fun serialize(encoder: Encoder, value: DateTime) {
		encoder.encodeString(value.toString(DateFormat.FORMAT2))
	}
}

@Serializable
class Event {
	var eventId: String? = null
	var engineId: String? = null
	// mandatory fields
	var event: String? = null
	var entityType: String? = null
	var entityId: String? = null

	// optional fields
	var targetEntityType: String? = null
	var targetEntityId: String? = null

	@Serializable(with = DateTimeSerializer::class)
	var eventTime: DateTime? = null

	@Serializable(with = DateTimeSerializer::class)
	var creationTime: DateTime? = null
}
