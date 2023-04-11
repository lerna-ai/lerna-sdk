package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.converter.DLArrayConverter
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsApi
import ai.lerna.multiplatform.service.dto.Success
import ai.lerna.multiplatform.service.dto.TrainingAccuracy
import ai.lerna.multiplatform.service.dto.TrainingInference
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import ai.lerna.multiplatform.service.dto.TrainingInitialize
import ai.lerna.multiplatform.service.dto.TrainingInitializeItem
import ai.lerna.multiplatform.service.dto.TrainingTasks
import ai.lerna.multiplatform.service.dto.TrainingWeights
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array


class FederatedLearningService(host: String, _token: String, _uniqueId: Long) {
	private val token = _token
	private var uniqueID = _uniqueId
	private val FL_API_URL = host
	private val dlArrayConverter = DLArrayConverter()

	private lateinit var trainingTasks: TrainingTasks

	private val client = HttpClient() {
		install(ContentNegotiation) {
			json(Json {
				prettyPrint = true
				isLenient = true
			})
		}
	}

	fun setTrainingTaskResponse(trainingTasks: TrainingTasks) {
		this.trainingTasks = trainingTasks
	}

	fun isTrainingTaskReady(version: Long = 0): Boolean {
		return this::trainingTasks.isInitialized
				&& trainingTasks.version!! > version
	}

	fun getTrainingTask(version: Long = 0): TrainingTasks? {
		if (isTrainingTaskReady(version)) {
			return trainingTasks
		}
		return null
	}

	private suspend fun requestNewTraining(): TrainingTasks? {
		try {
			val response = client.get(FL_API_URL + "training/new?token=" + token + "&deviceId=" + uniqueID)
			if (response.status != HttpStatusCode.OK) {
				Napier.d("LernaFLService - requestNewTraining Response error: ${response.bodyAsText()}", null, "Lerna")
				return null
			}
			return response.body()
		} catch (cause: Throwable) {
			Napier.d("LernaFLService - requestNewTraining deserialize exception: ${cause.message}", cause, "Lerna")
			return null
		}
	}

	suspend fun requestNewTraining(classes: MutableMap<String, MutableList<String>>?): TrainingTasks? {
		if (classes == null) {
			return requestNewTraining();
		}
		try {
			val request = TrainingInitialize()
			classes.forEach { ( model, job) -> request.classes.add(TrainingInitializeItem(modelName = model, jobs = job.toList())) }
			val response = client.post(FL_API_URL + "training/new?token=" + token + "&deviceId=" + uniqueID) {
				contentType(ContentType.Application.Json)
				setBody(request)
			}
			if (response.status != HttpStatusCode.OK) {
				Napier.d("LernaFLService - requestNewTraining Response error: ${response.bodyAsText()}", null, "Lerna")
				return null
			}
			return response.body()
		} catch (cause: Throwable) {
			Napier.d("LernaFLService - requestNewTraining deserialize exception: ${cause.message}", cause, "Lerna")
			return null
		}
	}

	suspend fun submitWeights(
		jobId: Long,
		version: Long,
		datapoints: Long,
		deviceWeights: D2Array<Float>
	): String? {
		val request = TrainingWeights()
		request.jobId = jobId
		request.deviceId = uniqueID
		request.version = version
		request.datapoints = datapoints
		request.deviceWeights = dlArrayConverter.convert(deviceWeights)
		val response = client.post(FL_API_URL + "training/submitWeights?token=" + token) {
			contentType(ContentType.Application.Json)
			setBody(request)
		}
		if (response.status != HttpStatusCode.OK) {
			Napier.d("LernaFLService - requestNewTraining Response error: ${response.bodyAsText()}", null, "Lerna")
			return null
		}
		return response.bodyAsText()
	}

	suspend fun requestNewWeights(version: Long): GlobalTrainingWeights? {
		try {
			val response = client.get(FL_API_URL + "training/getNewWeights?token=" + token + "&version=" + version)
			if (response.status != HttpStatusCode.OK) {
				Napier.d("LernaFLService - requestNewTraining Response error: ${response.bodyAsText()}", null, "Lerna")
				return null
			}
			if (response.bodyAsText().isEmpty()) {
				Napier.d("LernaFLService - requestNewTraining Response empty body", null, "Lerna")
				return null
			}
			val globalTrainingWeightsApi: GlobalTrainingWeightsApi = response.body()
			return dlArrayConverter.convert(globalTrainingWeightsApi)
		} catch (cause: Throwable) {
			Napier.d("LernaFLService - requestNewTraining deserialize exception: ${cause.message}", cause, "Lerna")
			return null
		}
	}

	suspend fun submitAccuracy(mlId: Long, version: Long, accuracy: Float): String? {
		val request = TrainingAccuracy()
		request.mlId = mlId
		request.deviceId = uniqueID
		request.version = version
		request.accuracy = accuracy
		val response = client.post(FL_API_URL + "training/accuracy?token=" + token) {
			contentType(ContentType.Application.Json)
			setBody(request)
		}
		if (response.status != HttpStatusCode.OK) {
			Napier.d("LernaFLService - submitAccuracy Response error: ${response.bodyAsText()}", null, "Lerna")
			return null
		}
		return response.bodyAsText()
	}

	suspend fun submitInference(version: Long, trainingInferenceItems: List<TrainingInferenceItem>, userIdentifier: String = ""): String? {
		val request = TrainingInference()
		request.deviceId = uniqueID
		request.userIdentifier = userIdentifier
		request.version = version
		request.trainingInference = trainingInferenceItems
		val response = client.post(FL_API_URL + "training/inference?token=" + token) {
			contentType(ContentType.Application.Json)
			setBody(request)
		}
		if (response.status != HttpStatusCode.OK) {
			Napier.d("LernaFLService - submitInference Response error: ${response.bodyAsText()}", null, "Lerna")
			return null
		}
		return response.bodyAsText()
	}

	suspend fun submitOutcome(version: Long, mlId: Long, prediction: String, success: String, position: String? = null): String? {
		val request = Success()
		request.ml_id = mlId
		request.version = version
		request.deviceId = uniqueID
		request.prediction = prediction
		request.success = success
		request.position = position
		val response = client.post(FL_API_URL + "training/success?token=" + token) {
			contentType(ContentType.Application.Json)
			setBody(request)
		}
		if (response.status != HttpStatusCode.OK) {
			Napier.d("LernaFLService - submitSuccess Response error: ${response.bodyAsText()}", null, "Lerna")
			return null
		}
		return response.bodyAsText()
	}
}
