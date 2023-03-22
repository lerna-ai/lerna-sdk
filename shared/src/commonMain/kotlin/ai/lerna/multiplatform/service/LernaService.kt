package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.ContextRunner
import ai.lerna.multiplatform.LernaConfig
import ai.lerna.multiplatform.ModelData
import ai.lerna.multiplatform.PeriodicRunner
import ai.lerna.multiplatform.SensorInterface
import ai.lerna.multiplatform.Sensors
import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import ai.lerna.multiplatform.utils.DateUtil
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.tempVfs
import com.soywiz.korio.stream.writeString
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class LernaService(private val context: KMMContext, _token: String, uniqueID: Long, _autoInference: Boolean) {
	private var flService: FederatedLearningService = FederatedLearningService(LernaConfig.FL_SERVER, _token, uniqueID)
	private val modelData: ModelData = ModelData(50)
	private var successValue = 0
	private var weights: GlobalTrainingWeights? = null
	private val fileUtil = FileUtil()
	private val storageService = StorageImpl(context)
	private val sensors: SensorInterface = Sensors(context, modelData)
	private var inferenceTasks: HashMap<Long, MLInference> = HashMap()
	private val periodicRunner = PeriodicRunner()
	private var autoInference = _autoInference

	private suspend fun commitToFile(record: String) {
		fileUtil.commitToFile(storageService.getSessionID(), record)
	}

	internal fun initCustomFeatureSize(size: Int) {
		modelData.setupCustomFeatureSize(size)
	}

	internal fun start() {
		sensors.start()
		this.weights = storageService.getWeights()
		weights?.trainingWeights?.forEach {
			val inferenceTask = MLInference()
			inferenceTask.setWeights(it)
			inferenceTasks[it.mlId!!] = inferenceTask
		}
		Napier.d("Start Periodic", null, "LernaService")
		periodicRunner.run(context, ::runPeriodic)
	}

	internal fun stop() {
		Napier.d("Stop Periodic", null, "LernaService")
		periodicRunner.stop()
		ContextRunner().runBlocking(context, ::sessionEnd)
		modelData.clearHistory()
	}

	internal fun updateFeatures(values: FloatArray) {
		modelData.updateCustomFeatures(values)
	}

	internal fun captureEvent(eventNumber: Int) {
		successValue = eventNumber
	}

	internal fun triggerInference() {
		if ((weights?.version ?: 0) > 0) {
			runBlocking {
				calcAndSubmitInference(mk.ndarray(arrayOf(modelData.toArray().map { it.toFloat() }.toFloatArray())))
			}
		} else {
			Napier.d("No weights yet from server", null, "LernaService")
		}
	}

	private suspend fun runPeriodic() {
		sensors.updateData()

		if (autoInference) {
			triggerInference()
		}

		if (LernaConfig.LOG_SENSOR_DATA) {
			Napier.d("Commit to history: ${storageService.getSessionID()},${DateUtil().now()},${modelData.toCsv()},$successValue\n", null, "LernaService")
		}
		if (successValue != 0) {
			if (!autoInference) {
				triggerInference()
			}
			val mlId = weights?.trainingWeights?.first { w -> w.mlName == storageService.getModelSelect() }?.mlId ?: -1
			flService.submitSuccess(weights!!.version, mlId, storageService.getLastInference() ?: "N/A", successValue.toString())
			sessionEnd()
		}
		successValue = 0 // Use success for only one session after event
	}

	private suspend fun sessionEnd() {
		var sessionId = storageService.getSessionID()
		commitToFile(modelData.historyToCsv(sessionId, successValue.toString()))
		Napier.d("Session $sessionId ended", null, "LernaService")
		sessionId++
		storageService.putSessionID(sessionId)

		weights?.trainingWeights?.forEach {
			inferenceTasks[it.mlId!!]?.clearHistory()
		}
	}

	internal suspend fun updateFileLastSession(sessionID: Int, successValue: Int) {
		try {
			val sensorFile = tempVfs["sensorLog$sessionID.csv"]
			val lines = sensorFile.readLines()
				.filter { it.isNotEmpty() }
				.map { it.replace(",0$".toRegex(), ",$successValue") }
				.toList()

			val sensorFileOutput = sensorFile.open(VfsOpenMode.CREATE_OR_TRUNCATE)
			for (line in lines) {
				sensorFileOutput.writeString("$line\n")
			}
			sensorFileOutput.close()
		} catch (e: Exception) {
			Napier.d("Log file not found: sensorLog$sessionID.csv", null, "LernaService")
		}
	}

	private suspend fun calcAndSubmitInference(dataArray: D2Array<Float>) {
		weights?.trainingWeights
			?.mapNotNull { it -> calcInference(dataArray, it) }
			?.let {
				if (it.isNotEmpty()) {
					storageService.putInference(it)
					Napier.d("Sending ${it.size} inference(s) to the DB", null, "LernaService")
					flService.submitInference(weights!!.version, it, storageService.getUserIdentifier() ?: "")
				}
			}
	}

	private fun calcInference(dataArray: D2Array<Float>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem? {
		var inference: TrainingInferenceItem? = TrainingInferenceItem()
		inference?.ml_id = weights.mlId!!
		inference?.model = weights.mlName
		inferenceTasks[weights.mlId!!]?.predictLabelFrom1Line1Item(dataArray)


		inference?.prediction = findMostCommonInList(inferenceTasks[weights.mlId!!]?.inferHistory)
		if (inference?.prediction == "0"
			|| inference?.prediction == "0.0"
			|| storageService.getLastInference() == inference?.prediction
			|| inference?.prediction == null
			|| weights.mlName != storageService.getModelSelect()
		) { //to only compute inference of the selected model
			inference = null
		} else {
			storageService.putLastInference(inference.prediction!!)
		}
		return inference
	}

	private fun <T> findMostCommonInList(list: MutableList<T>?): T? {
		return if (list != null) {
			list
				.groupBy { it }
				.maxByOrNull { it.value.size }
				?.key
		} else null
	}

	private fun concat(A: Array<DoubleArray>, B: Array<DoubleArray>, vararg X: Array<DoubleArray>): D2Array<Double>? {
		val mkA = mk.ndarray(A).transpose()
		val mkB = mk.ndarray(B).transpose()
		if(mkA.shape[1]!=mkB.shape[1])
			return null
		var output = mkA.flatten().cat(mkB.flatten())
		var totalColumns = mkA.shape[0]+mkB.shape[0]
		for (list in X) {
			val mkX = mk.ndarray(list).transpose()
			if(mkA.shape[1]!=mkX.shape[1])
				return null
			output = output.cat(mkX.flatten())
			totalColumns += mkX.shape[0]
		}
		return output.reshape(totalColumns, mkA.shape[1]).transpose()
	}
}
