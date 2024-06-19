package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.LernaConfig
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

	fun getABTestPer(): Float?

	fun putABTestPer(abtestper: Float)

	fun getLog(): Boolean

	fun putLog(enabled: Boolean)

	fun getSensorInitialDelay(): Long

	fun putSensorInitialDelay(delay: Int)

	fun getCustomFeaturesSize(): Int

	fun putCustomFeaturesSize(customFeaturesSize: Int)

	fun getInputDataSize(): Int

	fun putInputDataSize(inputDataSize: Int)

	fun getCleanupThreshold(): Int

	fun putCleanupThreshold(cleanupThreshold: Int)

	fun getTrainingSessionsThreshold(): Int

	fun putTrainingSessionsThreshold(threshold: Int)

	fun getActionMLEncryption(): Boolean

	fun putActionMLEncryption(enabled: Boolean)

	fun getEncryptionKey(): String?

	fun putEncryptionKey(encryptionKey: String)

}
