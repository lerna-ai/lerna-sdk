package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem

interface Storage {

	fun getWeights(): GlobalTrainingWeights?

	fun putWeights(trainingWeights: GlobalTrainingWeights?)

	fun getSessionID(): Int

	fun putSessionID(session: Int)

	fun getVersion(): Int

	fun putVersion(version: Int)

	fun getSize(): Int

	fun putSize(fileSize: Int)

	fun getInference(): MutableSet<String>?

	fun putInference(inference: List<TrainingInferenceItem>)

	fun getModelSelect(): String?

	fun putModelSelect(model: String)

	fun getLastInference(): String?

	fun putLastInference(lastInference: String)

	fun getLastTraining(): Int

	fun putLastTraining(trainingNumber: Int)

	fun getUserIdentifier(): String?

	fun putUserIdentifier(deviceToken: String)

}
