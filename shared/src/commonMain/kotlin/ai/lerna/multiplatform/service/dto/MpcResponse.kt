package ai.lerna.multiplatform.service.dto


@kotlinx.serialization.Serializable
class MpcResponse {
	var CompID: Long? = null
	var Share: Float? = null
	var Epsilon: Float? = null
}
