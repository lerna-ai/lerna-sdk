package ai.lerna.multiplatform.service.actionML.dto

import kotlinx.serialization.Serializable

@Serializable
class DatasetId {

	private var datasetId: String? = null

	fun getDatasetId(): String? {
		return datasetId
	}

	fun setDatasetId(datasetId: String?) {
		this.datasetId = datasetId
	}
}