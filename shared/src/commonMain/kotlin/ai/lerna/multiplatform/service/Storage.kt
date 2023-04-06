package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights

interface Storage {

	fun getWeights(): GlobalTrainingWeights?

	fun putWeights(trainingWeights: GlobalTrainingWeights?)

	fun getSessionID(): Int

	fun putSessionID(session: Int)

	fun getVersion(): Int

	fun putVersion(version: Int)

	fun getSize(): Int

	fun putSize(fileSize: Int)

	fun getTempInference(): String?

	fun putTempInference(lastInference: String)

	fun getLatestInference(): String?

	fun putLatestInference(lastInference: String)

	fun getLastTraining(): Int

	fun putLastTraining(trainingNumber: Int)

	fun getUserIdentifier(): String?

	fun putUserIdentifier(deviceToken: String)

	fun getUploadDataEnabled(): Boolean

	fun putUploadDataEnabled(enabled: Boolean)

}
