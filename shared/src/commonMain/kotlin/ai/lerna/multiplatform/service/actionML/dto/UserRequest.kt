package ai.lerna.multiplatform.service.actionML.dto

import kotlinx.serialization.Serializable

@Serializable
class UserRequest {
	var engineId: String? = null
	var num: Int? = null
	var user: String? = null
	var blacklistItems: List<String>? = null
	var rules: List<QueryRules>? = null
}