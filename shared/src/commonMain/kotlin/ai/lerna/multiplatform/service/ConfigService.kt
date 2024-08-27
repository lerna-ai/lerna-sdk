package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.LernaConfig
import ai.lerna.multiplatform.config.KMMContext
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
import kotlin.random.Random

class ConfigService(_context: KMMContext, _token: String, _uniqueId: Long) {
	private val token = _token
	private var uniqueID = _uniqueId
	private val storageService = StorageImpl(_context)

	private val client = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				prettyPrint = true
				isLenient = true
				ignoreUnknownKeys = true
			})
		}
	}

	internal suspend fun updateConfig() : Boolean {
		requestConfig()?.let { response ->
			response.mpcServerUri?.let { storageService.putMPCServer(it) }
			response.flServerUri?.let { storageService.putFLServer(it) }
			response.uploadPrefix?.let { storageService.putUploadPrefix(it) }
			response.uploadSensorData.let { storageService.putUploadDataEnabled(it) }
			response.logSensorData.let { storageService.putLog(it) }
			response.abTest.let {
				if (storageService.getABTestPer() != it) {
					storageService.putABTest(Random.nextFloat() < it)
					storageService.putABTestPer(it)
					Napier.d(
						"I am choosing ${if (storageService.getABTest()) "" else "non "}randomly ABTest",
						null,
						"Lerna"
					)
				}
			}
			response.customFeaturesSize.let { storageService.putCustomFeaturesSize(it) }
			response.inputDataSize.let {storageService.putInputDataSize(it) }
			response.sensorInitialDelay.let { storageService.putSensorInitialDelay(it) }
			response.trainingSessionsThreshold.let { storageService.putTrainingSessionsThreshold(it) }
			response.confidenceThreshold.let { storageService.putConfidenceThreshold(it) }
			response.cleanupThreshold.let { storageService.putCleanupThreshold(it) }
			response.actionMLEnabled.let { storageService.putActionMLEnabled(it) }
			response.actionMLEncryption.let {
				storageService.putActionMLEncryption(it)
				if (it) {
					MpcService(storageService.getMPCServer(), token).getEncryptionKey().let { encryption ->
						encryption.key?.let { key ->
							storageService.putEncryptionKey(key)
						}
					}
				}
			}
			return true
		} ?: run {
			return false
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
