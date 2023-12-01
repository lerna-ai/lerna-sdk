package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.MpcRequest
import ai.lerna.multiplatform.service.dto.MpcResponse
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class MpcService(host: String, _token: String) {
	private val token = _token
	private val mpcHost = host
	private val client = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				prettyPrint = true
				isLenient = true
				ignoreUnknownKeys = true
			})
		}
	}

	suspend fun lerna(compID: Long, userID: Long, size: Long): MpcResponse {
		val request = MpcRequest()
		request.compId = compID
		request.user = userID
		request.size = size

		val response = client.post("$mpcHost/mpc/call?token=$token") {
			contentType(ContentType.Application.Json)
			setBody(request)
		}
		Napier.d("LernaFLService - MPC Response: ${response.status}", null, "Lerna")
		if (response.status != HttpStatusCode.OK) {
			Napier.d(
				"LernaFLService - MPC Response error: ${response.bodyAsText()}",
				null,
				"Lerna"
			)
			return MpcResponse()
		}
		if (response.bodyAsText().isEmpty()) {
			Napier.d("LernaFLService - MPC Response empty body", null, "Lerna")
			return MpcResponse()
		}
		Napier.d("LernaFLService - MPC Response: ${response.bodyAsText()}", null, "Lerna")
		return response.body()
	}
}
