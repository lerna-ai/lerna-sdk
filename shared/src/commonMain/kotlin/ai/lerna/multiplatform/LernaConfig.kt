package ai.lerna.multiplatform

import kotlin.Boolean
import kotlin.String

internal object LernaConfig {
	internal val CONFIG_SERVER: String = "https://config.lerna.ai/api/v1/"

	internal val MPC_SERVER: String = "https://api.dev.lerna.ai:8080/"

	internal val FL_SERVER: String = "https://api.dev.lerna.ai:5000/api/v2/"

	internal val UPLOAD_PREFIX: String = "public/kmm/debug/"

	internal val LOG_SENSOR_DATA: Boolean = false

	internal val LOG_RECOMMENDATION_ENCRYPTION: Boolean = false
}
