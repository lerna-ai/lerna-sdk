package ai.lerna.multiplatform.service

import io.github.aakira.napier.Napier

class WeightsManager(token: String, uniqueID: Long) {
	private val federatedLearningService = FederatedLearningService("https://api.dev.lerna.ai:7357/api/v2/", token, uniqueID)
	private var weightsVersion = 0L
	private lateinit var storage: Storage
	// ToDo: Implement log uploader
	//private val logUploader: LogUploader = LogAwsUploaderImpl(context)

	var updateWeightsListener: ((String) -> Unit)? = null

	fun setupStorage(_storage: Storage) {
		storage = _storage
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
				var maxAccuracy = -1.0f
				trainingWeights.trainingWeights?.forEach() {
					Napier.d("Weights accuracies: ${it.accuracy.toString()} for ${it.mlName}", null, "LernaWeights")
					if (it.accuracy != null) {
						if (maxAccuracy < it.accuracy!!) {
							maxAccuracy = it.accuracy!!
							storage.putModelSelect(it.mlName ?: "")
						}
					}
				}
				Napier.d("Selected model for inferences: ${storage.getModelSelect()}", null, "LernaWeights")
				storage.putWeights(trainingWeights)
				storage.putTotalInferences(0.0F)
				storage.putSuccessRate(0.0F)
				//ml.setWeights(trainingWeights.trainingWeights!![0])
				weightsVersion = trainingWeights.version
			} else {
				Napier.d("Weights are up to date. Current version: $weightsVersion", null, "LernaWeights")
			}

			//logUploader.uploadLogcat(uniqueID, "logcatw.txt")
			return "Success"

		} catch (ex: Exception) {
			//logUploader.uploadLogcat(uniqueID, "logcat_errw.txt")
			return "Failed"
		}
	}
}