package ai.lerna.multiplatform.service.dto

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class GlobalTrainingWeightsItem {
	var weights // (prediction(lenra_job), weights(lerna_job))
			: Map<String, D2Array<Float>>? = null
	var mlId // database lerna_job.ml_id
			: Long? = null
	var mlName // database lerna_ml.model
			: String? = null
	var accuracy: Float? = null

	var weightsMultiKv2: Map<String, AdvancedMLItem>? = null

	var epochs: Int? = null

	var lr: Float? = null

	var dimentions: Int? = null

	var method: String? = null
}