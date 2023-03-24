package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.*
import ai.lerna.multiplatform.LernaConfig
import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import ai.lerna.multiplatform.utils.DateUtil
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array


class LernaService(private val context: KMMContext, _token: String, uniqueID: Long, _autoInference: Boolean) {
	private var flService: FederatedLearningService = FederatedLearningService(LernaConfig.FL_SERVER, _token, uniqueID)
	private val modelData: ModelData = ModelData(50)
	private var successValue = SUCCESS_INVALID
	private var weights: GlobalTrainingWeights? = null
	private val fileUtil = FileUtil()
	private val storageService = StorageImpl(context)
	private val sensors: SensorInterface = Sensors(context, modelData)
	private var inferenceTasks: HashMap<Long, MLInference> = HashMap()
	private val periodicRunner = PeriodicRunner()
	private var autoInference = _autoInference
	private var inferencesInSession = HashMap<String, String>() //reset on stop()?
	private var mergedInput = MergeInputData(modelData, 10) //initialize/create on start() for each position/inferenceID?

	internal companion object {
		const val SUCCESS_INVALID = -1
	}

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

				//choose one of the two
				calcAndSubmitInferenceMulItems("Top", mergedInput.getMergedInputData())
				calcAndSubmitInferenceMulItemsHistory("Top", mergedInput.getMergedInputDataHistory())
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
		if (successValue != SUCCESS_INVALID) {
			if (!autoInference) {
				triggerInference()
			}
			val mlId = weights?.trainingWeights?.first { w -> w.mlName == storageService.getModelSelect() }?.mlId ?: -1
			flService.submitOutcome(weights!!.version, mlId, storageService.getLastInference() ?: "N/A", successValue.toString())
			sessionEnd()
		}
		successValue = SUCCESS_INVALID // Use success for only one session after event
	}

	private suspend fun sessionEnd() {
		var sessionId = storageService.getSessionID()
		// 2 issues:
		//  1)how to take as input the itemID?
		//  2)Does mergedInput, when calling modelData, always get the latest history data? Or we need to create the mergedInput object every time?
		commitToFile(mergedInput.historyToCsv(sessionId, "top", successValue.toString()))
		Napier.d("Session $sessionId ended", null, "LernaService")
		sessionId++
		storageService.putSessionID(sessionId)

		weights?.trainingWeights?.forEach {
			inferenceTasks[it.mlId!!]?.clearHistory()
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

	private suspend fun calcAndSubmitInferenceMulItems(inferenceID: String, inputData: Pair<Array<String>, D2Array<Float>>) {
		//does the first "it" represent different ML models?
		//Are the ml models the prediction for like, comment etc. or are these different successes for the same model?!
		weights?.trainingWeights
			?.mapNotNull { it -> calcInferenceMulItems(inferenceID, inputData, it) }
			?.let {
				if (it.isNotEmpty()) {
					storageService.putInference(it)
					Napier.d("Sending ${it.size} inference(s) to the DB", null, "LernaService")
					flService.submitInference(weights!!.version, it, storageService.getUserIdentifier() ?: "")
				}
			}
	}

	private fun calcInferenceMulItems(inferenceID: String, inputData: Pair<Array<String>, D2Array<Float>>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem? {
		var inference: TrainingInferenceItem? = TrainingInferenceItem()
		inference?.ml_id = weights.mlId!!
		inference?.model = weights.mlName!!
		val scores = inferenceTasks[weights.mlId!!]?.predictLabelScore1LineMulItems(inputData, "like")
		val maxEntry = scores?.maxByOrNull { it.value }

		inferencesInSession[inferenceID] = maxEntry?.key ?: "-1"

		inference?.prediction = maxEntry?.key
		if (storageService.getLastInference() == inference?.prediction
			|| inference?.prediction == null
			|| weights.mlName != storageService.getModelSelect()
		) { //to only compute inference of the selected model - what do we select now?
			inference = null
		} else {
			storageService.putLastInference(inference.prediction!!)
		}
		return inference
	}

	private suspend fun calcAndSubmitInferenceMulItemsHistory(inferenceID: String, inputDataHistory: Map<String, D2Array<Float>>) {
		//does the first "it" represent different ML models?
		//Are the ml models the prediction for like, comment etc. or are these different successes for the same model?!
		weights?.trainingWeights
			?.mapNotNull { it -> calcInferenceMulItemsHistory(inferenceID, inputDataHistory, it) }
			?.let {
				if (it.isNotEmpty()) {
					storageService.putInference(it)
					Napier.d("Sending ${it.size} inference(s) to the DB", null, "LernaService")
					flService.submitInference(weights!!.version, it, storageService.getUserIdentifier() ?: "")
				}
			}
	}

	private fun calcInferenceMulItemsHistory(inferenceID: String, inputDataHistory: Map<String, D2Array<Float>>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem? {
		var inference: TrainingInferenceItem? = TrainingInferenceItem()
		inference?.ml_id = weights.mlId!!
		inference?.model = weights.mlName!!
		val scores = inferenceTasks[weights.mlId!!]?.predictLabelScoreMulLinesMulItems(inputDataHistory, "like")
		val maxEntry = scores?.maxByOrNull { it.value }

		inferencesInSession[inferenceID] = maxEntry?.key ?: "-1"

		inference?.prediction = maxEntry?.key
		if (storageService.getLastInference() == inference?.prediction
			|| inference?.prediction == null
			|| weights.mlName != storageService.getModelSelect()
		) { //to only compute inference of the selected model - what do we select now?
			inference = null
		} else {
			storageService.putLastInference(inference.prediction!!)
		}
		return inference
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

}
