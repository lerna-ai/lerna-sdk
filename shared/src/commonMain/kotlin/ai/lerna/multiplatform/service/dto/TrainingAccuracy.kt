package ai.lerna.multiplatform.service.dto

@kotlinx.serialization.Serializable
class TrainingAccuracy {
    var mlId: Long = 0
    var deviceId: Long = 0
    var version: Long = 0
    var accuracy: Double? = null
}