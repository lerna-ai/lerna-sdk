package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.KMMPreference
import ai.lerna.multiplatform.service.converter.DLArrayConverter
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsApi
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
	private val prefLastInference = "LernaLastInference"
	private val prefUserIdentifier = "LernaUserIdentifier"
	private val preUploadDataEnabled = "LernaUploadDataEnabled"
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
			sharedPref.put(prefWeightsID, "")
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

	override fun getLatestInference(): String? {
		if (!sharedPref.contains(prefLastInference)) {
			return ""
		}
		return sharedPref.getString(prefLastInference)
	}

	override fun putLatestInference(lastInference: String) {
		sharedPref.put(prefLastInference, lastInference)
	}

	override fun getTempInference(): String? {
		if (!sharedPref.contains(prefInference)) {
			return ""
		}
		return sharedPref.getString(prefInference)
	}

	override fun putTempInference(lastInference: String) {
		sharedPref.put(prefInference, lastInference)
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

	override fun getUserIdentifier(): String? {
		if (!sharedPref.contains(prefUserIdentifier)) {
			return null
		}
		return sharedPref.getString(prefUserIdentifier)
	}

	override fun putUserIdentifier(deviceToken: String) {
		sharedPref.put(prefUserIdentifier, deviceToken)
	}

	override fun getUploadDataEnabled(): Boolean {
		if (!sharedPref.contains(preUploadDataEnabled)) {
			return true
		}
		return sharedPref.getBool(preUploadDataEnabled, true)
	}

	override fun putUploadDataEnabled(enabled: Boolean) {
		sharedPref.put(preUploadDataEnabled, enabled)
	}
}
