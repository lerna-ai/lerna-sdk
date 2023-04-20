package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights

interface Storage {

	fun getMPCServer(): String

	fun putMPCServer(MPCServer: String)

	fun getFLServer(): String

	fun putFLServer(FLServer: String)

	fun getUploadPrefix(): String

	fun putUploadPrefix(uploadPrefix: String)

	fun getWeights(): GlobalTrainingWeights?

	fun putWeights(trainingWeights: GlobalTrainingWeights?)

	fun getClasses(): MutableMap<String, MutableList<String>>?

	fun putClasses(classes: MutableMap<String, MutableList<String>>)

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

	fun getABTest(): Boolean

	fun putABTest(enabled: Boolean)

	fun getLog(): Boolean

	fun putLog(enabled: Boolean)

}
