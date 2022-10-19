package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class GlobalTrainingWeightsApi {
	var version: Long = -1
	var trainingWeights: List<GlobalTrainingWeightsItemApi>? = null
}