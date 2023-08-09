package ai.lerna.multiplatform.service.actionML.dto

import kotlinx.serialization.Serializable

@Serializable
class QueryRules {
	var name: String? = null
	var values: List<String> = mutableListOf()
	var bias: Int? = null
}