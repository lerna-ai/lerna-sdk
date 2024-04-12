package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.LernaConfig
import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.KMMPreference
import ai.lerna.multiplatform.service.converter.DLArrayConverter
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsApi
import com.russhwolf.settings.contains
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StorageImpl(context: KMMContext) : Storage {
	private val prefWeightsID = "LernaWeights"
	private val prefSessionID = "LernaSession"
	private val prefVersion = "LernaVersion"
	private val prefTraining = "LernaLastTraining"
	private val prefClasses = "LernaClasses"
	private val prefSize = "LernaSize"
	private val prefInference = "LernaInference"
	private val prefLastInference = "LernaLastInference"
	private val prefConfidenceThreshold = "LernaConfidenceThreshold"
	private val prefUserIdentifier = "LernaUserIdentifier"
	private val preUploadDataEnabled = "LernaUploadDataEnabled"
	private val preABTestEnabled = "LernaABTestEnabled"
	private val preABTestPercent = "LernaABTestPercent"
	private val prefMPCServer = "LernaMPC"
	private val prefFLServer = "LernaFL"
	private val prefUploadPrefix = "LernaUploadPrefix"
	private val prefLogData = "LernaLog"
	private val prefSensorDelay = "LernaSensorDelay"
	private val prefCustomFeaturesSize = "LernaCustomFeaturesSize"
	private val prefInputDataSize = "LernaInputDataSize"
	private val prefCleanupThreshold = "LernaCleanupThreshold"
	private val prefTrainingSessionsThreshold = "LernaTrainingSessionsThreshold"
	private val prefActionMLEnabled = "LernaActionMLEnabled"
	private val prefActionMLEncryptionPrefix = "LernaActionMLEncryptionPrefix"
	private val prefEncryptionKeyPrefix = "LernaEncryptionKeyPrefix"
	private val sharedPref = KMMPreference(context)
	private val dlArrayConverter = DLArrayConverter()

	override fun getMPCServer(): String {
		if (!sharedPref.contains(prefMPCServer)) {
			return LernaConfig.MPC_SERVER
		}
		return sharedPref.getStringOrNull(prefMPCServer)?:LernaConfig.MPC_SERVER
	}

	override fun putMPCServer(MPCServer: String) {
		sharedPref.putString(prefMPCServer, MPCServer)
	}

	override fun getFLServer(): String {
		if (!sharedPref.contains(prefFLServer)) {
			return LernaConfig.FL_SERVER
		}
		return sharedPref.getStringOrNull(prefFLServer)?:LernaConfig.FL_SERVER
	}

	override fun putFLServer(FLServer: String) {
		sharedPref.putString(prefFLServer, FLServer)
	}

	override fun getUploadPrefix(): String {
		if (!sharedPref.contains(prefUploadPrefix)) {
			return LernaConfig.UPLOAD_PREFIX
		}
		return sharedPref.getStringOrNull(prefUploadPrefix)?:LernaConfig.UPLOAD_PREFIX
	}

	override fun putUploadPrefix(uploadPrefix: String) {
		sharedPref.putString(prefUploadPrefix, uploadPrefix)
	}

	override fun getWeights(): GlobalTrainingWeights? {
		if (!sharedPref.contains(prefWeightsID)) {
			return null
		}
		val weights = sharedPref.getStringOrNull(prefWeightsID) ?: "{}"
		val weightsObj = Json.decodeFromString<GlobalTrainingWeightsApi>(weights)
		return dlArrayConverter.convert(weightsObj)
	}

	override fun putWeights(trainingWeights: GlobalTrainingWeights?) {
		if (trainingWeights == null) {
			sharedPref.putString(prefWeightsID, "")
			return
		}
		val weightsApi = dlArrayConverter.convert(trainingWeights)
		val weights = Json.encodeToString(weightsApi)
		sharedPref.putString(prefWeightsID, weights)
	}

	override fun getClasses(): MutableMap<String, MutableList<String>>? {
		return if (!sharedPref.contains(prefClasses)) {
			null
		} else {
			Json.decodeFromString<MutableMap<String, MutableList<String>>>(sharedPref.getStringOrNull(prefClasses)!!)
		}
	}

	override fun putClasses(classes: MutableMap<String, MutableList<String>>) {
		sharedPref.putString(prefClasses, Json.encodeToString(classes))
	}

	override fun getSessionID(): Int {
		return sharedPref.getInt(prefSessionID, 0)
	}

	override fun putSessionID(session: Int) {
		sharedPref.putInt(prefSessionID, session)
	}

	override fun getSize(): Int {
		return sharedPref.getInt(prefSize, 0)
	}

	override fun putSize(fileSize: Int) {
		sharedPref.putInt(prefSize, fileSize)
	}

	override fun getLatestInference(): String? {
		if (!sharedPref.contains(prefLastInference)) {
			return ""
		}
		return sharedPref.getStringOrNull(prefLastInference)
	}

	override fun putLatestInference(lastInference: String) {
		sharedPref.putString(prefLastInference, lastInference)
	}

	override fun getConfidenceThreshold(): Float {
		if (!sharedPref.contains(prefConfidenceThreshold)) {
			return 0.5f
		}
		return sharedPref.getFloat(prefConfidenceThreshold, 0.5f)
	}

	override fun putConfidenceThreshold(confidence: Float) {
		sharedPref.put(prefConfidenceThreshold, confidence)
	}

	override fun getTempInference(): String? {
		if (!sharedPref.contains(prefInference)) {
			return ""
		}
		return sharedPref.getStringOrNull(prefInference)
	}

	override fun putTempInference(lastInference: String) {
		sharedPref.putString(prefInference, lastInference)
	}

	override fun getVersion(): Int {
		return sharedPref.getInt(prefVersion, 0)
	}

	override fun putVersion(version: Int) {
		sharedPref.putInt(prefVersion, version)
	}

	override fun getLastTraining(): Int {
		return sharedPref.getInt(prefTraining, 0)
	}

	override fun putLastTraining(trainingNumber: Int) {
		sharedPref.putInt(prefTraining, trainingNumber)
	}

	override fun getUserIdentifier(): String? {
		return sharedPref.getStringOrNull(prefUserIdentifier)
	}

	override fun putUserIdentifier(deviceToken: String) {
		sharedPref.putString(prefUserIdentifier, deviceToken)
	}

	override fun getUploadDataEnabled(): Boolean {
		return sharedPref.getBoolean(preUploadDataEnabled, true)
	}

	override fun putUploadDataEnabled(enabled: Boolean) {
		sharedPref.putBoolean(preUploadDataEnabled, enabled)
	}

	override fun getABTest(): Boolean {
		if (!sharedPref.contains(preABTestEnabled)) {
			return false
		}
		return sharedPref.getBoolean(preABTestEnabled, false)
	}

	override fun putABTestPer(abtestper: Float) {
		sharedPref.putFloat(preABTestPercent, abtestper)
	}

	override fun getABTestPer(): Float? {
		if (!sharedPref.contains(preABTestPercent)) {
			return null
		}
		return sharedPref.getFloat(preABTestPercent, 0.0f)
	}

	override fun putABTest(enabled: Boolean) {
		sharedPref.putBoolean(preABTestEnabled, enabled)
	}

	override fun getLog(): Boolean {
		if (!sharedPref.contains(prefLogData)) {
			return LernaConfig.LOG_SENSOR_DATA
		}
		return sharedPref.getBoolean(prefLogData, LernaConfig.LOG_SENSOR_DATA)
	}

	override fun putLog(enabled: Boolean) {
		sharedPref.putBoolean(prefLogData, enabled)
	}

	override fun getSensorInitialDelay(): Long {
		if (!sharedPref.contains(prefSensorDelay)) {
			return 0
		}
		return sharedPref.getInt(prefSensorDelay, 0).toLong()
	}

	override fun putSensorInitialDelay(delay: Int) {
		sharedPref.putInt(prefSensorDelay, delay)
	}

	override fun getCustomFeaturesSize(): Int {
		if (!sharedPref.contains(prefCustomFeaturesSize)) {
			return 0
		}
		return sharedPref.getInt(prefCustomFeaturesSize, 0)
	}

	override fun putCustomFeaturesSize(customFeaturesSize: Int) {
		sharedPref.putInt(prefCustomFeaturesSize, customFeaturesSize)
	}

	override fun getInputDataSize(): Int {
		if (!sharedPref.contains(prefInputDataSize)) {
			return 0
		}
		return sharedPref.getInt(prefInputDataSize, 0)
	}

	override fun putInputDataSize(inputDataSize: Int) {
		sharedPref.putInt(prefInputDataSize, inputDataSize)
	}

	override fun getCleanupThreshold(): Int {
		if (!sharedPref.contains(prefCleanupThreshold)) {
			return 10
		}
		return sharedPref.getInt(prefCleanupThreshold, 10)
	}

	override fun putCleanupThreshold(cleanupThreshold: Int) {
		sharedPref.putInt(prefCleanupThreshold, cleanupThreshold)
	}

	override fun getTrainingSessionsThreshold(): Int {
		if (!sharedPref.contains(prefTrainingSessionsThreshold)) {
			return 10
		}
		return sharedPref.getInt(prefTrainingSessionsThreshold, 10)
	}

	override fun putTrainingSessionsThreshold(threshold: Int) {
		sharedPref.putInt(prefTrainingSessionsThreshold, threshold.toInt())
	}

	override fun getActionMLEnabled(): Boolean {
		if (!sharedPref.contains(prefActionMLEnabled)) {
			return false
		}
		return sharedPref.getBoolean(prefActionMLEnabled, false)
	}

	override fun putActionMLEnabled(enabled: Boolean) {
		sharedPref.putBoolean(prefActionMLEnabled, enabled)
	}

	override fun getActionMLEncryption(): Boolean {
		if (!sharedPref.contains(prefActionMLEncryptionPrefix)) {
			return LernaConfig.LOG_RECOMMENDATION_ENCRYPTION
		}
		return sharedPref.getBoolean(prefActionMLEncryptionPrefix, LernaConfig.LOG_RECOMMENDATION_ENCRYPTION)
	}

	override fun putActionMLEncryption(enabled: Boolean) {
		sharedPref.putBoolean(prefActionMLEncryptionPrefix, enabled)
	}

	override fun getEncryptionKey(): String? {
		if (!sharedPref.contains(prefEncryptionKeyPrefix)) {
			return null
		}
		return sharedPref.getStringOrNull(prefEncryptionKeyPrefix)
	}

	override fun putEncryptionKey(encryptionKey: String) {
		sharedPref.putString(prefEncryptionKeyPrefix, encryptionKey)
	}


}
