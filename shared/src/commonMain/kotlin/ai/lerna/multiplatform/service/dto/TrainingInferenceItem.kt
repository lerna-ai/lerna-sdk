package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class TrainingInferenceItem {
    var ml_id: Long = 0
    var model: String? = null
    var prediction: String? = null
}