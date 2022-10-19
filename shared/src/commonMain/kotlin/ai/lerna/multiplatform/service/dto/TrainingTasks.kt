package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class TrainingTasks {
    var trainingTasks: List<TrainingTask>? = null
    var version: Long? = null
}