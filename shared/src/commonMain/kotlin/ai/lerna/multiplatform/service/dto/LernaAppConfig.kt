package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class LernaAppConfig {
	var mpcServerUri: String? = null

	var flServerUri: String? = null

	var uploadPrefix: String? = null

	var uploadSensorData: Boolean = true

	var logSensorData: Boolean = true

	var abTest: Float = 0.0f

	var customFeaturesSize: Int = 0

	var inputDataSize: Int = 0

	var sensorInitialDelay: Int = 0

	val trainingSessionsThreshold: Int = 10

	val confidenceThreshold: Float = 0.5f

	val cleanupThreshold: Int = 50000000

	var actionMLEnabled: Boolean = false

	var actionMLEncryption: Boolean = false
}
