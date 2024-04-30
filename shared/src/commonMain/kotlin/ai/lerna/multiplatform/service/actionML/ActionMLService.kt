package ai.lerna.multiplatform.service.actionML

import ai.lerna.multiplatform.service.actionML.dto.Event
import ai.lerna.multiplatform.service.actionML.dto.EventResponse
import ai.lerna.multiplatform.service.actionML.dto.ItemRequest
import ai.lerna.multiplatform.service.actionML.dto.QueryResponse
import ai.lerna.multiplatform.service.actionML.dto.QueryRules
import ai.lerna.multiplatform.service.actionML.dto.UserRequest
import com.soywiz.klock.DateTime
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ActionMLService(host: String, _token: String) {
	private val FL_API_URL = host
	private val token = _token
	private val client = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				prettyPrint = true
				isLenient = true
				ignoreUnknownKeys = true
			})
		}
	}

	suspend fun getUserItems(engineID: String, num: Int?, user: String?, blacklistItems: List<String>?, rules: List<QueryRules>?): QueryResponse {
		val request = UserRequest()
		request.engineId = engineID
		request.num = num
		request.user = user
		request.blacklistItems = blacklistItems
		request.rules = rules

		val response = client.post("$FL_API_URL/recommendation/queries?token=$token") {
			contentType(ContentType.Application.Json)
			setBody(request)
		}
		if (response.status != HttpStatusCode.OK
			&& response.status != HttpStatusCode.Created
			&& response.status != HttpStatusCode.Accepted
		) {
			Napier.d(
				"ActionMLService - Response error: ${response.bodyAsText()}",
				null,
				"Lerna"
			)
			return QueryResponse()
		}
		if (response.bodyAsText().isEmpty()) {
			Napier.d("ActionMLService - Response empty body", null, "Lerna")
			return QueryResponse()
		}
		return response.body()
	}

	suspend fun getItems(engineID: String, item: String?, itemSet: List<String>?, rules: List<QueryRules>?): QueryResponse {
		val request = ItemRequest()
		request.engineId = engineID
		request.item = item
		request.itemSet = itemSet
		request.rules = rules

		val response = client.post("$FL_API_URL/recommendation/queries?token=$token") {
			contentType(ContentType.Application.Json)
			setBody(request)
		}
		if (response.status != HttpStatusCode.OK
			&& response.status != HttpStatusCode.Created
			&& response.status != HttpStatusCode.Accepted
		) {
			Napier.d(
				"ActionMLService - Response error: ${response.bodyAsText()}",
				null,
				"Lerna"
			)
			return QueryResponse()
		}
		if (response.bodyAsText().isEmpty()) {
			Napier.d("ActionMLService - Response empty body", null, "Lerna")
			return QueryResponse()
		}
		return response.body()
	}

	suspend fun sendEvent(user: String, engineID: String, action: String, target: String, eventTime: DateTime): EventResponse {
		val request = Event()
		request.engineId = engineID
		request.event = action
		request.entityType = "user"
		request.entityId = user
		request.targetEntityType = "item"
		request.targetEntityId = target
		request.eventTime = eventTime

		val response = client.post("$FL_API_URL/recommendation/events?token=$token") {
			contentType(ContentType.Application.Json)
			setBody(request)
		}
		if (response.status != HttpStatusCode.OK
			&& response.status != HttpStatusCode.Created
			&& response.status != HttpStatusCode.Accepted
		) {
			Napier.d(
				"ActionMLService - Response error: ${response.bodyAsText()}",
				null,
				"Lerna"
			)
			return EventResponse()
		}
		if (response.bodyAsText().isEmpty()) {
			Napier.d("ActionMLService - Response empty body", null, "Lerna")
			return EventResponse()
		}
		return response.body()
	}
}
