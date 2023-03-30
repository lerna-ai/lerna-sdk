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
	private var data4Inference = mutableMapOf<String, MutableMap<String, FloatArray>>()
	private val successPositions = mutableMapOf<String, String>()
	private var weights: GlobalTrainingWeights? = null
	private val fileUtil = FileUtil()
	private val storageService = StorageImpl(context)
	private val sensors: SensorInterface = Sensors(context, modelData)
	private var inferenceTasks: HashMap<Long, MLInference> = HashMap()
	private val periodicRunner = PeriodicRunner()
	private var autoInference = _autoInference
	//here we store the articles+metadata per position
	private var inferencesInSession = mutableMapOf<String, MutableMap<String, FloatArray>>()
	private lateinit var mergedInput : MergeInputData 

	internal companion object {
		const val SUCCESS_INVALID = "-1"
	}

	private suspend fun commitToFile(record: String, filesPrefix: String) {
		fileUtil.commitToFile(storageService.getSessionID(), filesPrefix, record)
	}

	internal fun initCustomFeatureSize(size: Int) {
		modelData.setupCustomFeatureSize(size)
	}

	internal fun initInputSize(size: Int) {
		mergedInput = MergeInputData(modelData, size)
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
		ContextRunner().runBlocking(context, ::timeout)
		modelData.clearHistory()
	}

	internal fun updateFeatures(values: FloatArray) {
		modelData.updateCustomFeatures(values)
	}

	internal fun addInputData(itemId: String, values: FloatArray, positionID: String) {
		if(!data4Inference.containsKey(positionID)) {
			data4Inference[positionID] = mutableMapOf()
			//we also need to create the positions for the actual inferences/articles
			inferencesInSession[positionID] = mutableMapOf()
		}
		data4Inference[positionID]!![itemId] = values
	}

	internal fun captureEvent(event: String) {
		successValue = event
	}

	internal fun captureEvent(positionID: String, successVal: String) {
		successPositions[positionID] = successVal
		successValue = successVal
	}

	internal fun triggerInference(positionID: String, predictValue: String) : TrainingInferenceItem {
		if ((weights?.version ?: 0) > 0) {
			runBlocking {

				//choose one of the two
				calcAndSubmitInferenceMulItems(positionID, predictValue, mergedInput.getMergedInputData(data4Inference[positionID]!!))
				calcAndSubmitInferenceMulItemsHistory(positionID, predictValue, mergedInput.getMergedInputDataHistory(data4Inference[positionID]!!))

			}
		} else {
			Napier.d("No weights yet from server", null, "LernaService")
		}
	}

	private suspend fun runPeriodic() {
		sensors.updateData()

		//does this apply in the current setting with the data4Inference?
		if (autoInference) {
			data4Inference.keys.forEach {
				triggerInference(it, "success") //for automatic inference, how do we choose class?
			}
			data4Inference.clear()
		}

		if (LernaConfig.LOG_SENSOR_DATA) {
			Napier.d("Commit to history: ${storageService.getSessionID()},${DateUtil().now()},${modelData.toCsv()},$successValue\n", null, "LernaService")
		}
		if (successValue != SUCCESS_INVALID) {
			//why we trigger the inference after a success/failure happens?
			if (!autoInference) {
				data4Inference.keys.forEach {
					triggerInference(it, successValue) //successValue or something else?
				}
				data4Inference.clear()
			}
			val mlId = weights?.trainingWeights?.first { w -> w.mlName == storageService.getModelSelect() }?.mlId ?: -1
			if (successPositions.isEmpty()) {
				flService.submitOutcome(weights!!.version, mlId, storageService.getLastInference() ?: "N/A", successValue.toString())
			}
			else {
				successPositions.forEach { successPosition ->
					sessionEnd(successPosition.key, successPosition.value)
					inferencesInSession.remove(successPosition.key)
				}
				successPositions.clear()
			}

		}
		successValue = SUCCESS_INVALID // Use success for only one session after event
	}

	internal fun refresh() {
		Napier.d("Stop Periodic", null, "LernaService")
		periodicRunner.stop()
		ContextRunner().runBlocking(context, ::timeout)
		Napier.d("Start Periodic", null, "LernaService")
		periodicRunner.run(context, ::runPeriodic)
	}

	private suspend fun timeout() {
		successValue = "0"
		inferencesInSession.keys.forEach {
			sessionEnd(it, "success") //it shouldn't matter what we predicted as long as it is different?
		}
		inferencesInSession.clear()
		successValue = SUCCESS_INVALID // I saw it at the periodic and thought it should be here as well...
	}

	private suspend fun sessionEnd(positionID: String, predictValue: String) {
		var sessionId = storageService.getSessionID()
		// 2 issues:
		//  1)how to take as input the itemID?
		//  2)Does mergedInput, when calling modelData, always get the latest history data? Or we need to create the mergedInput object every time?
		val mlId = weights?.trainingWeights?.first { w -> w.mlName == storageService.getModelSelect() }?.mlId ?: -1
		//here we can add the ml_id for the file prefix to support different data for different mls
		commitToFile(mergedInput.historyToCsv(inferencesInSession[positionID]!!.entries.elementAt(0).value, sessionId, successValue), "sensorLog")
		Napier.d("Session $sessionId ended", null, "LernaService")
		flService.submitOutcome(weights!!.version, mlId, predictValue, successValue, positionID)
		sessionId++
		storageService.putSessionID(sessionId)

		//this now must be wrong...
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

	private suspend fun calcAndSubmitInferenceMulItems(positionID: String, predictValue: String, inputData: Pair<Array<String>, D2Array<Float>>) {
		//does the first "it" represent different ML models?
		//Are the ml models the prediction for like, comment etc. or are these different successes for the same model?!
		weights?.trainingWeights
			?.mapNotNull { it -> calcInferenceMulItems(positionID, predictValue, inputData, it) }
			?.let {
				if (it.isNotEmpty()) {
					storageService.putInference(it)
					Napier.d("Sending ${it.size} inference(s) to the DB", null, "LernaService")
					flService.submitInference(weights!!.version, it, storageService.getUserIdentifier() ?: "")
				}
			}
	}

	private fun calcInferenceMulItems(positionID: String, predictValue: String, inputData: Pair<Array<String>, D2Array<Float>>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem? {
		var inference: TrainingInferenceItem? = TrainingInferenceItem()
		inference?.ml_id = weights.mlId!!
		inference?.model = weights.mlName!!
		val scores = inferenceTasks[weights.mlId!!]?.predictLabelScore1LineMulItems(inputData, predictValue)
		val maxEntry = scores?.maxByOrNull { it.value }

		if(maxEntry?.key!=null)
			inferencesInSession[positionID]?.set(maxEntry.key, data4Inference[positionID]?.get(maxEntry.key)!!)

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

	private suspend fun calcAndSubmitInferenceMulItemsHistory(positionID: String, predictValue: String, inputDataHistory: Map<String, D2Array<Float>>) {
		//does the first "it" represent different ML models?
		//Are the ml models the prediction for like, comment etc. or are these different successes for the same model?!
		weights?.trainingWeights
			?.mapNotNull { it -> calcInferenceMulItemsHistory(positionID, predictValue, inputDataHistory, it) }
			?.let {
				if (it.isNotEmpty()) {
					storageService.putInference(it)
					Napier.d("Sending ${it.size} inference(s) to the DB", null, "LernaService")
					flService.submitInference(weights!!.version, it, storageService.getUserIdentifier() ?: "")
				}
			}
	}

	private fun calcInferenceMulItemsHistory(positionID: String, predictValue: String, inputDataHistory: Map<String, D2Array<Float>>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem? {
		var inference: TrainingInferenceItem? = TrainingInferenceItem()
		inference?.ml_id = weights.mlId!!
		inference?.model = weights.mlName!!
		val scores = inferenceTasks[weights.mlId!!]?.predictLabelScoreMulLinesMulItems(inputDataHistory, predictValue)
		val maxEntry = scores?.maxByOrNull { it.value }

		if(maxEntry?.key!=null)
			inferencesInSession[positionID]?.set(maxEntry.key, data4Inference[positionID]?.get(maxEntry.key)!!)

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
