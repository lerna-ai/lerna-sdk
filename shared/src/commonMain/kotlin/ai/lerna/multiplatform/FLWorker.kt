package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.FederatedLearningService
import ai.lerna.multiplatform.service.FileUtil
import ai.lerna.multiplatform.service.Storage
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.service.WeightsManager
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.cacheVfs
import com.soywiz.korio.stream.writeString
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ones
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class FLWorker {
	// ToDo: Update FL Service configuration
	private val federatedLearningService = FederatedLearningService("https://api.dev.lerna.ai:7357/api/v2/", "632523a5-bdf1-4241-8ec0-f8c8cd666050", 123L)
	private val weightsManager = WeightsManager()
	private val fileUtil = FileUtil()
	private var weightsVersion = -1L
	private var taskVersion = -1L
	private lateinit var storage: Storage
	private lateinit var context: KMMContext

	fun setupStorage(kmmContext: KMMContext) {
		context = kmmContext
		storage = StorageImpl(kmmContext)
		weightsManager.setupStorage(storage)
	}

	suspend fun startFL() = run {
		Napier.base(DebugAntilog())
		//CoroutineScope(Dispatchers.Main).launch {
		Napier.d("App Version: ${storage.getVersion()}", null, "LernaFL")
		val trainingTask = federatedLearningService.requestNewTraining() ?: return

		Napier.d("Task Version: ${trainingTask.version.toString()}", null, "LernaFL")

		taskVersion = trainingTask.version!!

		if (weightsManager.updateWeights() == "Success") {
			val globalWeights = storage.getWeights()
			if (globalWeights != null) {
				weightsVersion = globalWeights.version
			}

		}

		val jobId = trainingTask.trainingTasks?.get(0)?.jobIds?.get("news") ?: 1L
		val version = trainingTask.version ?: 2L
		val data: D2Array<Float> = mk.ones<Float>(100).reshape(2, 50)
		val submitResponse = federatedLearningService.submitWeights(jobId, version, 100L, data)
		Napier.d(submitResponse ?: "Error", null, "LernaStartFL")

		val weightVersion = version - 2
		val globalWeights = federatedLearningService.requestNewWeights(weightVersion)
		Napier.d(globalWeights.toString(), null, "LernaStartFL")

		storage.putWeights(globalWeights)

		val weightsFromStorage = storage.getWeights()
		val ver = storage.getInference()
		var tr1: TrainingInferenceItem = TrainingInferenceItem()
		tr1.ml_id = 1L
		tr1.model = "m1"
		tr1.prediction = "news"
		var tr2: TrainingInferenceItem = TrainingInferenceItem()
		tr2.ml_id = 2L
		tr2.model = "m2"
		tr2.prediction = "news"
		val tri = listOf(tr1, tr2)
		storage.putInference(tri)
		val ver2 = storage.getInference()
		Napier.d(ver.toString(), null, "LernaStartFL")
		Napier.d(ver2.toString(), null, "LernaStartFL")


		var sensorFile = cacheVfs["sensorLog0.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()
		sensorFile = cacheVfs["sensorLog0.csv"].open(VfsOpenMode.WRITE)
		sensorFile.setPosition(sensorFile.size())
		sensorFile.writeString("1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()


		sensorFile = cacheVfs["sensorLog2.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("1,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("2,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("3,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("4,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()

		storage.putSessionID(3)

		fileUtil.mergeFiles(storage)
		val mlData = cacheVfs["mldata.csv"].readLines().toList().filter { it.isNotEmpty() }

		Napier.d("Filesize: ${mlData.size}", null, "LernaFL")
		Napier.d("Data: ${mlData.toString()}", null, "LernaFL")

		val submitAccuracy = federatedLearningService.submitAccuracy(trainingTask?.trainingTasks?.get(0)?.mlId ?: 123L, version - 1, 100.0f)
		Napier.d(submitAccuracy ?: "Error", null, "LernaStartFL")

		val trainingInferenceItem = TrainingInferenceItem()
		trainingInferenceItem.ml_id = trainingTask?.trainingTasks?.get(0)?.mlId ?: 123L
		trainingInferenceItem.model = trainingTask?.trainingTasks?.get(0)?.mlModel ?: "Model"
		trainingInferenceItem.prediction = "news"
		val trainingInferenceItems: List<TrainingInferenceItem> = listOf(trainingInferenceItem)

		val submitInference = federatedLearningService.submitInference(version - 1, trainingInferenceItems,"123")
		Napier.d(submitInference ?: "Error", null, "LernaStartFL")
		Napier.d("FL finish", null, "LernaApp")

		//}

	}
}