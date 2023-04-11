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


class LernaService(private val context: KMMContext, _token: String, uniqueID: Long) {
	private var flService: FederatedLearningService = FederatedLearningService(LernaConfig.FL_SERVER, _token, uniqueID)
	private val modelData: ModelData = ModelData(50)
	private var autoInferenceValue : String? = null
	private var data4Inference = mutableMapOf<String, MutableMap<String, FloatArray>>()
	private var weights: GlobalTrainingWeights? = null
	private val fileUtil = FileUtil()
	private val storageService = StorageImpl(context)
	private val sensors: SensorInterface = Sensors(context, modelData)
	private var inferenceTasks: HashMap<Long, MLInference> = HashMap()
	private val periodicRunner = PeriodicRunner()
	private var inferencesInSession = mutableMapOf<String, MutableMap<String, FloatArray>>()
	private lateinit var mergedInput : MergeInputData 


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
		this.weights = storageService.getWeights()
		weights?.trainingWeights?.forEach {
			it.mlName?.let { it1 -> ContextRunner().runBlocking(context, it1, ::timeout) }
		}
		modelData.clearHistory()
	}

	internal fun updateFeatures(values: FloatArray) {
		modelData.updateCustomFeatures(values)
	}

	internal fun addInputData(itemId: String, values: FloatArray, positionID: String) {
		if(!data4Inference.containsKey(positionID)) {
			data4Inference[positionID] = mutableMapOf()
			inferencesInSession[positionID] = mutableMapOf()
		}
		data4Inference[positionID]!![itemId] = values
	}


	internal fun captureEvent(modelName: String, positionID: String, event: String) {
		runBlocking {
			val classes = storageService.getClasses()
			if(classes!=null) {
				if (classes.containsKey(modelName)) {
					if (!classes[modelName]!!.contains(event)) {
						classes[modelName]!!.add(event)
						storageService.putClasses(classes)
					}
				} else {
					classes[modelName] = mutableListOf(event)
					storageService.putClasses(classes)
				}
			} else {
					val temp: MutableMap<String, MutableList<String>> =
						mutableMapOf(Pair(modelName, mutableListOf(event)))
					storageService.putClasses(temp)
			}
			sessionEnd(modelName, positionID, event, event)
			inferencesInSession.remove(positionID)
		}
	}

	//if predictionClass == null, choose best class
	internal fun triggerInference(modelName: String, positionID: String?, predictionClass: String?) : String? {
		if ((weights?.version ?: 0) > 0 && weights?.trainingWeights?.find { it.mlName == modelName }!=null) {
			runBlocking {
				//if we have a specific prediction to make (e.g., like, comment, ...)
				if(predictionClass!=null) {
					//then we must have a position for the output of this position, i.e., object metadata
					if(positionID!=null) {
						//remember to choose one of the two!!
						calcAndSubmitInferenceMulItems(
							modelName,
							positionID,
							predictionClass,
							mergedInput.getMergedInputData(data4Inference[positionID]!!)
						)
						//////////////////////////
						calcAndSubmitInferenceMulItemsHistory(
							modelName,
							positionID,
							predictionClass,
							mergedInput.getMergedInputDataHistory(data4Inference[positionID]!!)
						)
						//////////////////////////
					}
				} else { //if we do not have a specific prediction to make, then only for 1 item:
					if(positionID==null) { //do not use metadata, just use the sensor and custom data (mostly for the autoInference, but you never know...)
						calcAndSubmitInference(
							modelName,
							mergedInput.lastLineD2Array()
						)
					}
				}
			}
			return storageService.getTempInference() //make sure that every path writes the tempInference
		} else {
			Napier.d("No weights yet from server for model $modelName", null, "LernaService")
			return null
		}
	}

	//Setting on/off to set the autoInference on and off for the specific model.
	//Limitations for now:
	// 1) we cannot have positions, meaning you can have autoinference in general, not for different elements
	// 2) We can use the custom features, but not additional metadata tight to a position
	// 3) We do not get the pedictionClass as input and as such, we ony pick the best class to return
	// 4) ONLY 1 MODEL SUPPORTED!
	//Of course, if needed we can change that, but probably we need another structure with positions and data
	internal fun setAutoInference(modelName: String, setting: String) : Boolean {
		if(setting == "on") {
			if ((weights?.version
					?: 0) > 0 && weights?.trainingWeights?.find { it.mlName == modelName } != null
			) {
				autoInferenceValue = modelName
				return true
			} else {
				Napier.d("No weights yet from server for model $modelName", null, "LernaService")
				return false
			}
		} else if(setting == "off"){
			autoInferenceValue = null
			return true
		} else {
			Napier.d("Wrong setting $setting", null, "LernaService")
			return false
		}
	}

	private suspend fun runPeriodic() {
		sensors.updateData()
		if (LernaConfig.LOG_SENSOR_DATA) {
			Napier.d("Commit to history: ${storageService.getSessionID()},${DateUtil().now()},${modelData.toCsv()}\n", null, "LernaService")
		}
		if(autoInferenceValue!=null){
			triggerInference(autoInferenceValue!!, null, null)
		}
	}

	internal fun refresh(modelName: String) {
		Napier.d("Stop Periodic", null, "LernaService")
		periodicRunner.stop()
		ContextRunner().runBlocking(context, modelName, ::timeout)
		Napier.d("Start Periodic", null, "LernaService")
		periodicRunner.run(context, ::runPeriodic)
	}

	private suspend fun timeout(modelName: String) {
		inferencesInSession.keys.forEach {
			sessionEnd(modelName, it, "success", "failure") //it shouldn't matter what we predicted as long as it is different?
		}
		inferencesInSession.clear()
	}

	private suspend fun sessionEnd(modelName: String, positionID: String, predictValue: String, successValue: String) {
		var sessionId = storageService.getSessionID()
		val mlId = weights?.trainingWeights?.first { w -> w.mlName == modelName }?.mlId ?: -1
		//here we can add the ml_id for the file prefix to support different data for different mls
		commitToFile(mergedInput.historyToCsv(inferencesInSession[positionID]!!.entries.elementAt(0).value, sessionId, successValue), "sensorLog")
		Napier.d("Session $sessionId ended", null, "LernaService")
		flService.submitOutcome(weights!!.version, mlId, predictValue, successValue, positionID)
		sessionId++
		storageService.putSessionID(sessionId)
	}


	private suspend fun calcAndSubmitInference(modelName: String, dataArray: D2Array<Float>) {
		val item = weights?.trainingWeights?.find { it.mlName == modelName }
			?.let { calcInference(dataArray, it) }
		if(item!=null) {
			Napier.d("Sending inference to the DB", null, "LernaService")
			flService.submitInference(
				weights!!.version,
				listOf(item),
				storageService.getUserIdentifier() ?: ""
			)
		}
	}

	private fun calcInference(dataArray: D2Array<Float>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem? {
		var inference: TrainingInferenceItem? = TrainingInferenceItem()
		inference?.ml_id = weights.mlId!!
		inference?.model = weights.mlName
		inference?.prediction = inferenceTasks[weights.mlId!!]?.predictLabelFrom1Line1Item(dataArray)

		storageService.putTempInference(inference?.prediction!!)

		if(autoInferenceValue == weights.mlName) { //this is only for the autoInference so that we do not bombard the server with inferences.
			// Also the reason we do not support now more than 1 autoInferences: where to store the latestInference per model?
			if (storageService.getLatestInference() == inference.prediction
				|| inference.prediction == null
			) {
				inference = null
			} else {
				storageService.putLatestInference(inference.prediction!!)
			}
		}

		return inference
	}

	private suspend fun calcAndSubmitInferenceMulItems(modelName: String, positionID: String, predictionClass: String, inputData: Pair<Array<String>, D2Array<Float>>) {
		val item = weights?.trainingWeights?.find { it.mlName == modelName }
			?.let { calcInferenceMulItems(positionID, predictionClass, inputData, it) }
		if(item!=null) {
			Napier.d("Sending inference to the DB", null, "LernaService")
			flService.submitInference(
				weights!!.version,
				listOf(item),
				storageService.getUserIdentifier() ?: ""
			)
		}
	}

	private fun calcInferenceMulItems(positionID: String, predictionClass: String, inputData: Pair<Array<String>, D2Array<Float>>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem {
		val inference = TrainingInferenceItem()
		inference.ml_id = weights.mlId!!
		inference.model = weights.mlName!!
		val scores = inferenceTasks[weights.mlId!!]?.predictLabelScore1LineMulItems(inputData, predictionClass)
		val maxEntry = scores?.maxByOrNull { it.value }

		if(maxEntry?.key!=null)
			inferencesInSession[positionID]?.set(maxEntry.key, data4Inference[positionID]?.get(maxEntry.key)!!)

		inference.prediction = maxEntry?.key
		storageService.putTempInference(inference.prediction!!)

		return inference
	}

	private suspend fun calcAndSubmitInferenceMulItemsHistory(modelName: String, positionID: String, predictionClass: String, inputDataHistory: Map<String, D2Array<Float>>) {
		val item = weights?.trainingWeights?.find { it.mlName == modelName }
			?.let { calcInferenceMulItemsHistory(positionID, predictionClass, inputDataHistory, it) }
		if(item!=null) {
			Napier.d("Sending inference to the DB", null, "LernaService")
			flService.submitInference(
				weights!!.version,
				listOf(item),
				storageService.getUserIdentifier() ?: ""
			)
		}
	}

	private fun calcInferenceMulItemsHistory(positionID: String, predictionClass: String, inputDataHistory: Map<String, D2Array<Float>>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem {
		val inference = TrainingInferenceItem()
		inference.ml_id = weights.mlId!!
		inference.model = weights.mlName!!
		val scores = inferenceTasks[weights.mlId!!]?.predictLabelScoreMulLinesMulItems(inputDataHistory, predictionClass)
		val maxEntry = scores?.maxByOrNull { it.value }

		if(maxEntry?.key!=null)
			inferencesInSession[positionID]?.set(maxEntry.key, data4Inference[positionID]?.get(maxEntry.key)!!)

		inference.prediction = maxEntry?.key

		storageService.putTempInference(inference.prediction!!)

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
