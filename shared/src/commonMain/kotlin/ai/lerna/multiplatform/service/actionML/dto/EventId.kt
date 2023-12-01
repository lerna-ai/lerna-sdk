package ai.lerna.multiplatform.service.actionML.dto

import kotlinx.serialization.Serializable

@Serializable
class EventId {

	private var eventId: String? = null

	fun getEventId(): String? {
		return eventId
	}

	fun setEventId(eventId: String?) {
		this.eventId = eventId
	}
}