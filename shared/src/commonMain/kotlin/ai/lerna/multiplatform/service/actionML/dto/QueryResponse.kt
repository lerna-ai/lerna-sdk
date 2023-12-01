package ai.lerna.multiplatform.service.actionML.dto

import kotlinx.serialization.Serializable

@Serializable
class QueryResponse {
	var result: List<Result>? = null
}