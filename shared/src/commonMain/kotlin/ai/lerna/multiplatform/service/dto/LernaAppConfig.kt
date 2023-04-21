package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class LernaAppConfig {
	var mpcServerUri: String? = null

	var flServerUri: String? = null

	var uploadPrefix: String? = null

	var logSensorData: Boolean = true

	var abTest: Double = 0.0
}