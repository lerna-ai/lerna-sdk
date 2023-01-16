package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.ModelData
import ai.lerna.multiplatform.SensorInterface
import ai.lerna.multiplatform.Sensors
import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.cacheVfs
import com.soywiz.korio.stream.writeString
import io.github.aakira.napier.Napier
import io.ktor.util.date.*
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LernaService(private val context: KMMContext) {
	private lateinit var flService: FederatedLearningService
	private val modelData: ModelData = ModelData()
	private var successValue = 0
	private var weights: GlobalTrainingWeights? = null
	private val fileUtil = FileUtil()
	private val storageService = StorageImpl(context)
	private val sensors: SensorInterface = Sensors(context, modelData)
	private var inferenceTasks: HashMap<Long, MLInference> = HashMap()
	private var runPeriodicFlag = false

	private suspend fun commitToFile(record: String) {
		fileUtil.commitToFile(storageService.getSessionID(), record)
	}

	fun start() {
		sensors.start()
		this.weights = storageService.getWeights()
		runPeriodicFlag = true
		Napier.d("Start Periodic", null, "LernaService")
		//ToDo: Add periodic call to runPeriodic()
		//ToDO: initialize flService
//		CoroutineScope(Dispatchers.Default).launch {
//			PeriodicRunner().run(context, runPeriodic)
//		}
		CoroutineScope(Dispatchers.Default).launch {
			while(runPeriodicFlag) {
				delay(2000)
				runPeriodic()
			}
		}
	}

	fun stop() {
		Napier.d("Stop Periodic", null, "LernaService")
		runPeriodicFlag = false
	}

	suspend fun runPeriodic() {
		sensors.updateData()

		if ((weights?.version ?: 0) > 0)
			calcAndSubmitInference(mk.ndarray(arrayOf(modelData.toArray().map { it.toFloat() }.toFloatArray())))
		else
			Napier.d("No weights yet from server", null, "LernaService")

		val time = GMTDate().toCustomDate()
		//commitToFile("${storageService.getSessionID()},$time,${modelData.toCsv()},$successValue\n")
		Napier.d("Commit to file: ${storageService.getSessionID()},$time,${modelData.toCsv()},$successValue\n", null, "LernaService")
		if (successValue != 0) {
			updateFileLastSession(storageService.getSessionID(), successValue)
			sessionEnd(successValue)
		}
		successValue = 0 // Use success for only one session after event
	}

	private suspend fun sessionEnd(successValue: Int) {
		var sessionId = storageService.getSessionID()
		Napier.d("Session $sessionId ended", null, "LernaService")
		sessionId++
		storageService.putSessionID(sessionId)
		fileUtil.switchFile(sessionId)
		modelData.resetSensorHistory()

//		weights?.trainingWeights?.forEach {
//			val maxVal = inferenceTasks[it.mlId!!]?.inferHistory?.stream()
//				?.collect(Collectors.groupingBy(identity, Collectors.counting()))
//				?.entries?.stream()?.max { o1, o2 -> o1.value.compareTo(o2.value) }
//				?.map { value -> value.key }?.orElse(null)
//
//			if (successValue != 0 && it.mlName == storageService.getModelSelect()) { //in order to use only 1 ml task
//				if (maxVal != null) {
//					if (storageService.getSuccesses() != null) {
//						storageService.putTotalInferences(storageService.getTotalInferences() + 1.0F)
//						val temp = storageService.getSuccesses()!!.toList().sorted().takeLast(199).toMutableSet()
//						if (maxVal == successValue)
//							storageService.putSuccessRate(storageService.getSuccessRate() + 1.0F)
//						val successRate = 100.0 * storageService.getSuccessRate() / storageService.getTotalInferences()
//						val time = LocalDateTime.now()
//						val app = storageService.getLastApp()
//						if (app?.split('.')!!.size > 1)
//							temp.add("$time,$maxVal,$successValue,${app.split('.')[app.split('.').size - 2]}.${app.split('.')[app.split('.').size - 1]},$successRate,${it.mlName},${weights?.version}")
//						else
//							temp.add("$time,$maxVal,$successValue,$app,$successRate,${it.mlName},${weights?.version}")
//						storageService.putSuccesses(temp)
//					} else {
//						storageService.putTotalInferences(1.0F)
//						storageService.putSuccessRate(0.0F)
//						if (maxVal == successValue)
//							storageService.putSuccessRate(1.0F)
//						val successRate = 100.0 * storageService.getSuccessRate()
//						val time = LocalDateTime.now()
//						val temp: MutableSet<String> = mutableSetOf()
//						val app = storageService.getLastApp()
//						if (app?.split('.')!!.size > 1)
//							temp.add("$time,$maxVal,$successValue,${app.split('.')[app.split('.').size - 2]}.${app.split('.')[app.split('.').size - 1]},$successRate,${it.mlName},${weights?.version}")
//						else
//							temp.add("$time,$maxVal,$successValue,$app,$successRate,${it.mlName},${weights?.version}")
//						storageService.putSuccesses(temp)
//					}
//				}
//			}
//			inferenceTasks[it.mlId!!]?.clearHistory()
//		}
	}

	internal suspend fun updateFileLastSession(sessionID: Int, successValue: Int) {
		try {
			val sensorFile = cacheVfs["sensorLog$sessionID.csv"]
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

		var inferenceList: List<TrainingInferenceItem?>? = weights?.trainingWeights?.map { it -> calcInference(dataArray, it)}


		inferenceList = inferenceList?.filterNotNull()


		// ToDo: for future use, currently response is empty
//		flService.submitInferenceListener= { submitInference ->
//			Log.d("LernaService", submitInference)
//		}
		if (inferenceList != null) {
			if (inferenceList.isNotEmpty()) {
				storageService.putInference(inferenceList)
				Napier.d("Sending ${inferenceList.size} inference(s) to the DB", null, "LernaService")
				flService.submitInference(weights!!.version, inferenceList, storageService.getUserIdentifier()?: "")

			}
		}
	}

	private fun calcInference(dataArray: D2Array<Float>, weights: GlobalTrainingWeightsItem): TrainingInferenceItem? {
		var inference: TrainingInferenceItem? = TrainingInferenceItem()
		inference?.ml_id = weights.mlId!!
		inference?.model = weights.mlName
		inferenceTasks[weights.mlId!!]?.predictLabel(dataArray)


		inference?.prediction = findMostCommonInList(inferenceTasks[weights.mlId!!]?.inferHistory)
		if (inference?.prediction == "0" || inference?.prediction == "0.0" || storageService.getLastInference() == inference?.prediction || inference?.prediction == null || weights.mlName != storageService.getModelSelect()) { //to only compute inference of the selected model
			inference = null
		} else {
			storageService.putLastInference(inference.prediction!!)
		}
		return inference
	}

	private fun <T>findMostCommonInList(list: MutableList<T>?) : T? {
		return if (list != null) {
			list
				.groupBy { it }
				.maxByOrNull { it.value.size }
				?.key
		} else null
	}

	private fun GMTDate.toCustomDate(): String = buildString {
		append(year.padZero(4))
		append("-${(month.ordinal + 1).padZero(2)}")
		append("-${dayOfMonth.padZero(2)}")
		append("_${hours.padZero(2)}.${minutes.padZero(2)}.${seconds.padZero(2)}")
	}

	private fun Int.padZero(length: Int): String = toString().padStart(length, '0')
}
