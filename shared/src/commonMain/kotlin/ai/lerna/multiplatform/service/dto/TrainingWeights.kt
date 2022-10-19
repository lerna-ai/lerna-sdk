package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class TrainingWeights {
	var jobId: Long = 0
	var deviceId: Long = 0
	var version: Long = 0
	var datapoints: Long = 0
	var deviceWeights: DoubleArray = DoubleArray(0)
}