package ai.lerna.multiplatform.service.dto

@kotlinx.serialization.Serializable
class Success {
	var ml_id: Long? = null
	var version: Long = 0
	var deviceId: Long = 0
	var prediction: String? = null
	var success: String? = null
	var position: Long? = null
}
