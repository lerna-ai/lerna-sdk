package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class GlobalTrainingWeightsItemApi {
	var weights // (prediction(lenra_job), weights(lerna_job))
			: Map<String, DoubleArray>? = null
	var mlId // database lerna_job.ml_id
			: Long? = null
	var mlName // database lerna_ml.model
			: String? = null
	var accuracy: Double? = null
}