package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class MpcRequest {
	var compId: Long? = null
	var user: Long? = null
	var size: Long? = null
}
