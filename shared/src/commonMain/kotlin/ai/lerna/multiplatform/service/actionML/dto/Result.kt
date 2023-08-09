package ai.lerna.multiplatform.service.actionML.dto

import kotlinx.serialization.Serializable

@Serializable
class Result {
	var item: String? = null
	var score: Float? = null
	var props: Map<String, List<String>>? = null
}