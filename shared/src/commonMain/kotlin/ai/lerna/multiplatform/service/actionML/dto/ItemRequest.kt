package ai.lerna.multiplatform.service.actionML.dto

import kotlinx.serialization.Serializable

@Serializable
class ItemRequest {
	var engineId: String? = null
	var item: String? = null
	var itemSet: List<String>? = null
	var rules: List<QueryRules>? = null
}