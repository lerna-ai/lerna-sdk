package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.KMMPreference
import ai.lerna.multiplatform.service.converter.DLArrayConverter
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsApi
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StorageImpl(context: KMMContext) : Storage {
	private val prefWeightsID = "LernaWeights"
	private val prefSessionID = "LernaSession"
	private val prefVersion = "LernaVersion"
	private val prefTraining = "LernaLastTraining"
	private val prefSize = "LernaSize"
	private val prefInference = "LernaInference"
	private val prefSuccesses = "LernaSuccesses"
	private val prefSuccessRate = "LernaSuccessRate"
	private val prefLastApp = "LernaLastApp"
	private val prefModelSelect = "LernaModelSelect"
	private val prefLastInference = "LernaLastInference"
	private val prefTotalInferences = "LernaTotalInferences"
	private val prefUserIdentifier = "LernaUserIdentifier"
	private val sharedPref: KMMPreference = KMMPreference(context)
	private val dlArrayConverter = DLArrayConverter()

	override fun getWeights(): GlobalTrainingWeights? {
		if (!sharedPref.contains(prefWeightsID)) {
			return null
		}
		val weights = sharedPref.getString(prefWeightsID) ?: "{}"
		val weightsObj = Json.decodeFromString<GlobalTrainingWeightsApi>(weights)
		return dlArrayConverter.convert(weightsObj)
	}

	override fun putWeights(trainingWeights: GlobalTrainingWeights?) {
		if (trainingWeights == null) {
			sharedPref.put(prefWeightsID, null as String)
			return
		}
		val weightsApi = dlArrayConverter.convert(trainingWeights)
		val weights = Json.encodeToString(weightsApi)
		sharedPref.put(prefWeightsID, weights)
	}

	override fun getSessionID(): Int {
		if (!sharedPref.contains(prefSessionID)) {
			return 0
		}
		return sharedPref.getInt(prefSessionID, 0)
	}

	override fun putSessionID(session: Int) {
		sharedPref.put(prefSessionID, session)
	}

	override fun getSize(): Int {
		if (!sharedPref.contains(prefSize)) {
			return 0
		}
		return sharedPref.getInt(prefSize, 0)
	}

	override fun putSize(fileSize: Int) {
		sharedPref.put(prefSize, fileSize)
	}

	override fun getSuccessRate(): Float {
		if (!sharedPref.contains(prefSuccessRate)) {
			return 0.0F
		}
		return sharedPref.getFloat(prefSuccessRate, 0.0F)
	}

	override fun putSuccessRate(successRate: Float) {
		sharedPref.put(prefSuccessRate, successRate)
	}

	override fun getTotalInferences(): Float {
		if (!sharedPref.contains(prefTotalInferences)) {
			return 0.0F
		}
		return sharedPref.getFloat(prefTotalInferences, 0.0F)
	}

	override fun putTotalInferences(totalInferences: Float) {
		sharedPref.put(prefTotalInferences, totalInferences)
	}

	override fun getLastApp(): String? {
		if (!sharedPref.contains(prefLastApp)) {
			return ""
		}
		return sharedPref.getString(prefLastApp)
	}

	override fun putLastApp(lastApp: String) {
		sharedPref.put(prefLastApp, lastApp)
	}

	override fun getModelSelect(): String? {
		if (!sharedPref.contains(prefModelSelect)) {
			return ""
		}
		return sharedPref.getString(prefModelSelect)
	}

	override fun putModelSelect(model: String) {
		sharedPref.put(prefModelSelect, model)
	}

	override fun getLastInference(): String? {
		if (!sharedPref.contains(prefLastInference)) {
			return ""
		}
		return sharedPref.getString(prefLastInference)
	}

	override fun putLastInference(lastInference: String) {
		sharedPref.put(prefLastInference, lastInference)
	}

	override fun getVersion(): Int {
		if (!sharedPref.contains(prefVersion)) {
			return 0
		}
		return sharedPref.getInt(prefVersion, 0)
	}

	override fun putVersion(version: Int) {
		sharedPref.put(prefVersion, version)
	}

	override fun getLastTraining(): Int {
		if (!sharedPref.contains(prefTraining)) {
			return 0
		}
		return sharedPref.getInt(prefTraining, 0)
	}

	override fun putLastTraining(trainingNumber: Int) {
		sharedPref.put(prefTraining, trainingNumber)
	}

	override fun getInference(): MutableSet<String>? {
		if (!sharedPref.contains(prefInference)) {
			return null
		}
		return sharedPref.getArray(prefInference)?.toMutableSet()
	}

	override fun putInference(inference: List<TrainingInferenceItem>) {
		sharedPref.put(prefInference, inference.map { Json.encodeToString(it) }.toTypedArray())
	}

	override fun getSuccesses(): MutableSet<String>? {
		if (!sharedPref.contains(prefSuccesses)) {
			return null
		}
		return sharedPref.getArray(prefSuccesses)?.toMutableSet()
	}

	override fun putSuccesses(successes: MutableSet<String>?) {

		sharedPref.put(prefSuccesses, successes?.toTypedArray() as Array<String>)
	}

	override fun getUserIdentifier(): String? {
		if (!sharedPref.contains(prefUserIdentifier)) {
			return null
		}
		return sharedPref.getString(prefUserIdentifier)
	}

	override fun putUserIdentifier(deviceToken: String) {
		sharedPref.put(prefUserIdentifier, deviceToken)
	}
}
