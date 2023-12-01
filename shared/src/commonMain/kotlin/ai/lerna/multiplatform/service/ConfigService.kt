package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.LernaConfig
import ai.lerna.multiplatform.service.dto.LernaAppConfig
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ConfigService(_token: String, _uniqueId: Long) {
	private val token = _token
	private var uniqueID = _uniqueId

	private val client = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				prettyPrint = true
				isLenient = true
				ignoreUnknownKeys = true
			})
		}
	}

	internal suspend fun requestConfig(): LernaAppConfig? {
		try {
			val response = client.get(LernaConfig.CONFIG_SERVER + "config/app?token=" + token + "&deviceId=" + uniqueID)
			if (response.status != HttpStatusCode.OK) {
				Napier.d("ConfigService - requestConfig Response error: ${response.bodyAsText()}", null, "Lerna")
				return null
			}
			return response.body()
		} catch (cause: Throwable) {
			Napier.d("ConfigService - requestConfig deserialize exception: ${cause.message}", cause, "Lerna")
			return null
		}
	}
}
