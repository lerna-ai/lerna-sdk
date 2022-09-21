package ai.lerna.multiplatform.service.dto

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

@kotlinx.serialization.Serializable
class TrainingWeights {
    var jobId: Long = 0
    var deviceId: Long = 0
    var version: Long = 0
    var datapoints: Long = 0
    var deviceWeights: D2Array<Double>? = null
}