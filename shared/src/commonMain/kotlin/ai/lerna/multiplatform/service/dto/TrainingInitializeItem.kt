package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class TrainingInitializeItem {

	constructor(modelName: String?, jobs: List<String>?) {
		this.modelName = modelName
		this.jobs = jobs
	}

	var modelName: String? = null

	var jobs: List<String>? = null
}