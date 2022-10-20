package ai.lerna.multiplatform

import ai.lerna.multiplatform.service.FederatedLearningService
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ones
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class FLWorker {
	private val federatedLearningService = FederatedLearningService("https://api.dev.lerna.ai:7357/api/v2/", "632523a5-bdf1-4241-8ec0-f8c8cd666050",123L)
	private var weightsVersion = -1L
	private var taskVersion = -1L

	fun startFL() = runBlocking {
		Napier.base(DebugAntilog())
		launch {
			val trainingTask = federatedLearningService.requestNewTraining() ?: return@launch

			taskVersion = trainingTask.version!!

			Napier.d("Task Version: ${trainingTask.version.toString()}", null, "LernaFL")

			val jobId = trainingTask?.trainingTasks?.get(0)?.jobIds?.get("news") ?: 1L
			val version = trainingTask?.version ?: 2L
			val data: D2Array<Float> = mk.ones<Float>(100).reshape(2, 50)
			val submitResponse = federatedLearningService.submitWeights(jobId, version, 100L, data)
			Napier.d(submitResponse?: "Error", null, "LernaStartFL")

			val weightVersion = version - 2
			val globalWeights = federatedLearningService.requestNewWeights(weightVersion)
			Napier.d(globalWeights.toString(), null, "LernaStartFL")

			val submitAccuracy = federatedLearningService.submitAccuracy(trainingTask?.trainingTasks?.get(0)?.mlId ?: 123L, version-1, 100.0f)
			Napier.d(submitAccuracy?: "Error", null, "LernaStartFL")

			val trainingInferenceItem = TrainingInferenceItem()
			trainingInferenceItem.ml_id = trainingTask?.trainingTasks?.get(0)?.mlId ?: 123L
			trainingInferenceItem.model = trainingTask?.trainingTasks?.get(0)?.mlModel ?: "Model"
			trainingInferenceItem.prediction = "news"
			val trainingInferenceItems : List<TrainingInferenceItem> = listOf(trainingInferenceItem)

			val submitInference = federatedLearningService.submitInference(version-1, trainingInferenceItems,"123")
			Napier.d(submitInference?: "Error", null, "LernaStartFL")
		}
	}
}