package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class TrainingTask {
    var jobIds: Map<String, Long>? = null

    var mlId: Long? = null

    var mlModel: String? = null

    var lernaMLParameters: LernaMLParameters? = null
}