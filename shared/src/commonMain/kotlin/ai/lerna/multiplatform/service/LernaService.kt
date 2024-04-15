package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.*
import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import ai.lerna.multiplatform.utils.DateUtil
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array


class LernaService(private val context: KMMContext, _token: String, uniqueID: Long) {
	private val modelData: ModelData = ModelData(50)
	private var autoInferenceValue : String? = null
	private var data4Inference = mutableMapOf<String, MutableMap<String, FloatArray>>()
	private var weights: GlobalTrainingWeights? = null
	private val fileUtil = FileUtil()
	private val storageService = StorageImpl(context)
	private var flService: FederatedLearningService = FederatedLearningService(storageService.getFLServer(), _token, uniqueID)
	private val sensors: SensorInterface = Sensors(context, modelData)
	private var inferenceTasks: HashMap<Long, MLInference> = HashMap()
	private val periodicRunner = PeriodicRunner()
	private var inferencesInSession = mutableMapOf<String, MutableMap<String, FloatArray>>()
	private lateinit var mergedInput : MergeInputData
	private var failsafe = mutableMapOf<String, String>()
	private val sensorLogEnabled = storageService.getLog()
	private val autoInferenceCounterDefaultValue = 4 // Get auto inference every 2 seconds with cycle of 500ms
	private var autoInferenceCounter = autoInferenceCounterDefaultValue


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
		try {
			sensors.start()
			this.weights = storageService.getWeights()
			weights?.trainingWeights?.forEach {
				val inferenceTask = MLInference()
				inferenceTask.setWeights(it)
				inferenceTasks[it.mlId!!] = inferenceTask
			}
			Napier.d("Start Periodic", null, "LernaService")
			periodicRunner.run(context, storageService.getSensorInitialDelay(), ::runPeriodic)
		}
		catch (e: Exception) {
			Napier.d("Start method failed: ${e.message}", e, "LernaService")
		}
	}

	internal fun stop() {
		Napier.d("Stop Periodic", null, "LernaService")
		periodicRunner.stop()
		this.weights = storageService.getWeights()
		weights?.trainingWeights?.forEach {
			it.mlName?.let { modelName -> ContextRunner().runBlocking(context, modelName, ::timeout) }
		}
		modelData.clearHistory()
	}

	internal fun updateFeatures(values: FloatArray) {
		modelData.updateCustomFeatures(values)
	}

	internal fun addInputData(itemId: String, values: FloatArray, positionID: String, disabled: Boolean = false) {
		if(!disabled) {
			if (!data4Inference.containsKey(positionID)) {
				data4Inference[positionID] = mutableMapOf()
				inferencesInSession[positionID] = mutableMapOf()
			}
			data4Inference[positionID]!![itemId] = values
		} else {
			failsafe[positionID] = itemId
		}
	}


	internal fun captureEvent(modelName: String, positionID: String, event: String, elementID: String = "") {
		if ((weights?.version
				?: 0) > 0 && weights?.trainingWeights?.find { it.mlName == modelName } != null
		) {
			runBlocking {
				val classes = storageService.getClasses()
				if (classes != null) {
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
				if (inferencesInSession.containsKey(positionID) && inferencesInSession[positionID]!!.isNotEmpty()) {
					sessionEnd(modelName, positionID, event, event, storageService.getABTest(), elementID)
					inferencesInSession.remove(positionID)
				} else {
					Napier.d(
						"Wrong position or event already captured!",
						null,
						"LernaService"
					)
				}
			}
		} else {
			Napier.d("CaptureEvent: No weights yet from server for model $modelName", null, "LernaService")
		}
	}

	//if predictionClass == null, choose best class
	internal fun triggerInference(modelName: String, positionID: String?, predictionClass: String?, disabled: Boolean = false, numElements: Int = 1) : String? {
		if(!disabled) {
			if ((weights?.version
					?: 0) > 0 && weights?.trainingWeights?.find { it.mlName == modelName } != null
			) { if(predictionClass==null || inferenceTasks[weights?.trainingWeights?.find { it.mlName == modelName }?.mlId]?.thetaClass!!.containsKey(predictionClass)) {
				runBlocking {
					//if we have a specific prediction to make (e.g., like, comment, ...)
					if (predictionClass != null) {
						//then we must have a position for the output of this position, i.e., object metadata
						if (positionID != null) {
							//remember to choose one of the two!!
//						calcAndSubmitInferenceMulItems(
//							modelName,
//							positionID,
//							predictionClass,
//							mergedInput.getMergedInputData(data4Inference[positionID]!!),
//							storageService.getABTest()
//						)
//						data4Inference.remove(positionID)
							//////////////////////////
							var retries = 0
							while (!modelData.isHistoryNonEmpty() && retries < 100) {
								delay(20)
								retries++
							}
							if (retries > 0) {
								Napier.d(
									"Waiting $retries times for sensor data",
									null,
									"LernaService"
								)
							}
							if (data4Inference.contains(positionID) && data4Inference[positionID]!!.isNotEmpty() && modelData.isHistoryNonEmpty()) {
								calcAndSubmitInferenceMulItemsHistory(
									modelName,
									positionID,
									predictionClass,
									mergedInput.getMergedInputDataHistory(data4Inference[positionID]!!),
									storageService.getABTest(), numElements
								)
								data4Inference.remove(positionID)
							} else {
								Napier.d(
									"Cannot run inference without sensor and/or content data",
									null,
									"LernaService"
								)
							}
							//////////////////////////
						}
					} else { //if we do not have a specific prediction to make, then only for 1 item:
						if (positionID == null) { //do not use metadata, just use the sensor and custom data (mostly for the autoInference, but you never know...)
							if (modelData.isHistoryNonEmpty()) {
								calcAndSubmitInference(
									modelName,
									mergedInput.lastLineD2Array()
								)
							} else {
								Napier.d(
									"Cannot run inference without sensor data",
									null,
									"LernaService"
								)
							}
						} else {
							Napier.d(
								"Cannot run inference when there is no specific prediction for this position",
								null,
								"LernaService"
							)
						}
					}
				}
				return storageService.getTempInference() //make sure that every path writes the tempInference
			} else {
				Napier.d("No prediction class $predictionClass for $modelName", null, "LernaService")
				return null
			}
			} else {
				Napier.d("TriggerInference: No weights yet from server for model $modelName", null, "LernaService")
				return null
			}
		} else {
			return failsafe.remove(positionID)
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
		when (setting) {
			"on" -> {
				return if ((weights?.version
						?: 0) > 0 && weights?.trainingWeights?.find { it.mlName == modelName } != null
				) {
					autoInferenceValue = modelName
					true
				} else {
					Napier.d("AutoInference: No weights yet from server for model $modelName", null, "LernaService")
					false
				}
			}
			"off" -> {
				autoInferenceValue = null
				return true
			}
			else -> {
				Napier.d("Wrong setting $setting. Choose between on and off", null, "LernaService")
				return false
			}
		}
	}

	private suspend fun runPeriodic() {
		sensors.updateData()
		if (sensorLogEnabled) {
			Napier.d("Commit to history: ${storageService.getSessionID()},${DateUtil().now()},${modelData.toCsv()}\n", null, "LernaService")
		}
		if (autoInferenceValue != null) {
			autoInferenceCounter--
			if (autoInferenceCounter == 0) {
				autoInferenceCounter = autoInferenceCounterDefaultValue
				triggerInference(autoInferenceValue!!, null, null)
			}
		}
	}

	internal fun refresh(modelName: String) {
		if ((weights?.version
				?: 0) > 0 && weights?.trainingWeights?.find { it.mlName == modelName } != null
		) {
			Napier.d("Stop Periodic", null, "LernaService")
			periodicRunner.stop()
			ContextRunner().runBlocking(context, modelName, ::timeout)
			Napier.d("Start Periodic", null, "LernaService")
			periodicRunner.run(context, storageService.getSensorInitialDelay(), ::runPeriodic)
		} else {
			Napier.d("Refresh: No weights yet from server for model $modelName", null, "LernaService")
		}
	}

	private suspend fun timeout(modelName: String) {
		inferencesInSession.filter { it.value.isNotEmpty() }.keys.forEach {
			sessionEnd(modelName, it, "success", "failure", storageService.getABTest()) //it shouldn't matter what we predicted as long as it is different?
		}
		inferencesInSession.clear()
		data4Inference.clear()
	}

	private suspend fun sessionEnd(modelName: String, positionID: String, predictValue: String, successValue: String, ABTest: Boolean = false, elementID: String = "") {
		var sessionId = storageService.getSessionID()
		val mlId = weights?.trainingWeights?.firstOrNull  { w -> w.mlName == modelName }?.mlId ?: -1
		//here we can add the ml_id for the file prefix to support different data for different mls
		if(elementID.isEmpty()) {
			ContextRunner().run(
				context,
				mergedInput.historyToCsv(
					inferencesInSession[positionID]!!.entries.first().value,
					sessionId,
					successValue
				),
				"sensorLog",
				::commitToFile
			)
		} else if (inferencesInSession[positionID]!!.containsKey(elementID)){
			ContextRunner().run(
				context,
				mergedInput.historyToCsv(inferencesInSession[positionID]!![elementID]!!, sessionId, successValue),
				"sensorLog",
				::commitToFile)
		} else {
			Napier.d("ERROR - Session $sessionId ended with wrong elementID: $elementID", null, "LernaService")
		}
		Napier.d("Session $sessionId ended", null, "LernaService")
		if(ABTest)
			flService.submitOutcome(weights!!.version, mlId, "$modelName-Random", predictValue, successValue, positionID)
		else
			flService.submitOutcome(weights!!.version, mlId, modelName, predictValue, successValue, positionID)
		sessionId++
		storageService.putSessionID(sessionId)
	}


	private suspend fun calcAndSubmitInference(modelName: String, dataArray: D2Array<Float>) {
		val item = weights?.trainingWeights?.find { it.mlName == modelName }
			?.let { calcInference(dataArray, it) }
		if(item!=null && autoInferenceValue == modelName) {
			try {
				Napier.d("Sending inference to the DB... ", null, "LernaService")
				flService.submitInference(
					weights!!.version,
					listOf(item),
					storageService.getUserIdentifier() ?: ""
				)
			} catch(e: Exception){
				Napier.d("Error! +${e.message}", null, "LernaService")
			}
			Napier.d("Done!", null, "LernaService")
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

	private suspend fun calcAndSubmitInferenceMulItems(modelName: String, positionID: String, predictionClass: String, inputData: Pair<Array<String>, D2Array<Float>>, pickRandom: Boolean = false, numElements: Int = 1) {
		val item = weights?.trainingWeights?.find { it.mlName == modelName }
			?.let { calcInferenceMulItems(positionID, predictionClass, inputData, it, pickRandom, numElements) }
		if(item!=null && autoInferenceValue == modelName) {
			try {
				Napier.d("Sending inference to the DB... ", null, "LernaService")
				flService.submitInference(
					weights!!.version,
					listOf(item),
					storageService.getUserIdentifier() ?: ""
				)
			} catch(e: Exception){
				Napier.d("Error! +${e.message}", null, "LernaService")
			}
			Napier.d("Done!", null, "LernaService")
		}
	}

	private fun calcInferenceMulItems(positionID: String, predictionClass: String, inputData: Pair<Array<String>, D2Array<Float>>, weights: GlobalTrainingWeightsItem, pickRandom: Boolean, numElements: Int = 1): TrainingInferenceItem {
		val inference = TrainingInferenceItem()
		inference.ml_id = weights.mlId!!
		inference.model = if(!pickRandom) {weights.mlName!!} else {weights.mlName!!+"-Random"}
		val scores = inferenceTasks[weights.mlId!!]?.predictLabelScore1LineMulItems(inputData, predictionClass)

		val topEntries = if(!pickRandom) {
			scores?.toList()?.sortedBy { (_, value) -> value}
		} else {
			scores?.toList()
		}

		//val maxEntry = if(!pickRandom) {scores?.maxByOrNull { it.value }} else {scores?.entries?.elementAt(Random.nextInt(scores.size))}
		val predictions  = ArrayList<String>()
		//if(maxEntry?.key!=null)
		for(item in topEntries?.take(if(topEntries.size>numElements) numElements else topEntries.size)!!)
		{
			inferencesInSession[positionID]?.set(
				item.first,
				data4Inference[positionID]?.get(item.first)!!
			)
			predictions.add(item.first)
		}

		inference.prediction = predictions.joinToString(separator = "|")

		inference.prediction?.let { storageService.putTempInference(it) }

		return inference
	}

	private suspend fun calcAndSubmitInferenceMulItemsHistory(modelName: String, positionID: String, predictionClass: String, inputDataHistory: Map<String, D2Array<Float>>, pickRandom: Boolean = false, numElements: Int = 1) {
		val item = weights?.trainingWeights?.find { it.mlName == modelName }
			?.let { calcInferenceMulItemsHistory(positionID, predictionClass, inputDataHistory, it, pickRandom, numElements) }

		if(item!=null && autoInferenceValue == modelName) {
			try {
				Napier.d("Sending inference to the DB... ", null, "LernaService")
				flService.submitInference(
					weights!!.version,
					listOf(item),
					storageService.getUserIdentifier() ?: ""
				)
			} catch(e: Exception){
				Napier.d("Error! +${e.message}", null, "LernaService")
			}
			Napier.d("Done!", null, "LernaService")
		}
	}

	private fun calcInferenceMulItemsHistory(positionID: String, predictionClass: String, inputDataHistory: Map<String, D2Array<Float>>, weights: GlobalTrainingWeightsItem, pickRandom: Boolean, numElements: Int = 1): TrainingInferenceItem {
		val inference = TrainingInferenceItem()
		inference.ml_id = weights.mlId!!
		inference.model = if(!pickRandom) {weights.mlName!!} else {weights.mlName!!+"-Random"}
		val scores = inferenceTasks[weights.mlId!!]?.predictLabelScoreMulLinesMulItems(inputDataHistory, predictionClass)
		//val maxEntry = if(!pickRandom) {scores?.maxByOrNull { it.value }} else {scores?.entries?.elementAt(Random.nextInt(scores.size))}
		val topEntries = if(!pickRandom) {
			scores?.toList()?.sortedBy { (_, value) -> value}
		} else {
			scores?.toList()
		}
		val predictions  = ArrayList<String>()
		for(item in topEntries?.take(if(topEntries.size>numElements) numElements else topEntries.size)!!)
		{
			inferencesInSession[positionID]?.set(
				item.first,
				data4Inference[positionID]?.get(item.first)!!
			)
			predictions.add(item.first)
		}
		inference.prediction = predictions.joinToString(separator = "|")

		inference.prediction?.let { storageService.putTempInference(it) }

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

	fun clearInputData(positionID: String) {
		data4Inference.remove(positionID)
	}

}
