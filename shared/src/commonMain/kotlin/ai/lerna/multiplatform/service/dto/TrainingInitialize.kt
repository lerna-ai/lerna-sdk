package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class TrainingInitialize {
	var classes: MutableList<TrainingInitializeItem> = mutableListOf()
}