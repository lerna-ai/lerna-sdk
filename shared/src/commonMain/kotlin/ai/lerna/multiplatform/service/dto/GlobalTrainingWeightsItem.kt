package ai.lerna.multiplatform.service.dto

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class GlobalTrainingWeightsItem {
	var weights // (prediction(lenra_job), weights(lerna_job))
			: Map<String, D2Array<Double>>? = null
	var mlId // database lerna_job.ml_id
			: Long? = null
	var mlName // database lerna_ml.model
			: String? = null
	var accuracy: Double? = null
}