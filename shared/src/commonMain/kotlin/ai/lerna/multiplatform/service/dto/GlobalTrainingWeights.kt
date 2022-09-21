package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class GlobalTrainingWeights {
    var version: Long = -1
    var trainingWeights: List<GlobalTrainingWeightsItem>? = null
}