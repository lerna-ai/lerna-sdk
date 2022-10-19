package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class TrainingInference {
    var deviceId: Long = 0
    var userIdentifier: String? = null
    var version: Long = 0
    var trainingInference: List<TrainingInferenceItem>? = null
}