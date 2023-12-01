package ai.lerna.multiplatform.service

import io.github.aakira.napier.Napier

class WeightsManager(_token: String, _uniqueID: Long) {
	private lateinit var federatedLearningService: FederatedLearningService
	private var weightsVersion = 0L
	private val token = _token
	private val uniqueID = _uniqueID
	private lateinit var storage: Storage

	var updateWeightsListener: ((String) -> Unit)? = null

	fun setupStorage(_storage: Storage) {
		storage = _storage
		federatedLearningService = FederatedLearningService(storage.getFLServer(), token, uniqueID)
	}

	suspend fun updateWeights(): String {
		Napier.d("App Version: ${storage.getVersion()}", null, "LernaWeights")
		try {
			val globalWeights = storage.getWeights()
			if (globalWeights != null) {
				weightsVersion = globalWeights.version
			}
			Napier.d("Version Request: $weightsVersion", null, "LernaWeights")

			val trainingWeights = federatedLearningService.requestNewWeights(weightsVersion)
			if (trainingWeights !== null) {
				Napier.d("New Weights Version: ${trainingWeights.version}", null, "LernaWeights")
				//var maxAccuracy = -1.0f
				trainingWeights.trainingWeights?.forEach {
					Napier.d("Weights accuracies: ${it.accuracy.toString()} for ${it.mlName}", null, "LernaWeights")
//					if (it.accuracy != null) {
//						if (maxAccuracy < it.accuracy!!) {
//							maxAccuracy = it.accuracy!!
//							storage.putModelSelect(it.mlName ?: "")
//						}
//					}
				}
//				Napier.d("Selected model for inferences: ${storage.getModelSelect()}", null, "LernaWeights")
				storage.putWeights(trainingWeights)
				//ml.setWeights(trainingWeights.trainingWeights!![0])
				weightsVersion = trainingWeights.version
			} else {
				Napier.d("Weights are up to date. Current version: $weightsVersion", null, "LernaWeights")
			}

			return "Success"

		} catch (ex: Exception) {
			return "Failed"
		}
	}
}