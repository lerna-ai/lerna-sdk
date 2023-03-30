package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.UserID
import ai.lerna.multiplatform.service.FileUtil
import ai.lerna.multiplatform.service.LernaService
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.service.WeightsManager
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking

class Lerna(context: KMMContext, token: String, customFeaturesSize: Int = 0, autoInference: Boolean = true) {
	private val _context = context
	private val _customFeaturesSize = customFeaturesSize
	private var _inputDataSize = 0
	private var _token = token
	private val uniqueID = UserID().getUniqueId(_context).toLong()
	private val storageService = StorageImpl(_context)
	private val weightsManager = WeightsManager(token, uniqueID)
	private val flWorker = FLWorkerInterface(_context)
	private val lernaService = LernaService(_context, _token, uniqueID, autoInference)

	internal companion object {
		const val FEATURE_SIZE = 46 // Lerna features plus x0
	}

	init {
		Napier.base(DebugAntilog())
		Napier.d("Initialize library", null, "Lerna")
		weightsManager.setupStorage(storageService)
		runBlocking {
			weightsManager.updateWeights()
		}
		if (checkWeightSize()) {
			runFL()
		}
	}

	fun setInputSize(size: Int) {
		if (_inputDataSize != 0) {
			throw IllegalArgumentException("The input size already set.")
		}
		if (size <= 0) {
			throw IllegalArgumentException("Invalid input size.")
		}
		_inputDataSize = size
		lernaService.initInputSize(size)
	}

	fun start() {
		if (checkWeightSize()) {
			runCleanUp()
			initialize()
		}
		else {
			Napier.d("Incorrect feature size, library disabled!", null, "Lerna")
		}
	}

	fun stop() {
		lernaService.stop()
	}

	fun setUserIdentifier(userID: String) {
		storageService.putUserIdentifier(userID)
	}

	fun captureEvent(event: LernaEvent = LernaEvent.SUCCESS) {
		captureEvent(event.value)
	}

	fun captureEvent(eventNumber: Int) {
		validateEventNumber(eventNumber)
		lernaService.captureEvent(eventNumber)
	}

	fun captureEvent(itemID: String) {
		lernaService.captureEvent(itemID)
	}

	fun updateFeature(values: FloatArray) {
		if (values.size != _customFeaturesSize) {
			throw IllegalArgumentException("Incorrect feature size")
		}
		lernaService.updateFeatures(values)
	}

	fun addInputData(itemID: String, values: FloatArray) {
		if (values.size != _inputDataSize) {
			throw IllegalArgumentException("Incorrect input data size")
		}
		lernaService.addInputData(itemID, values)
	}

	fun triggerInference() {
		lernaService.triggerInference()
	}

	fun enableUserDataUpload(enable: Boolean) {
		storageService.putUploadDataEnabled(enable)
	}

	private fun checkWeightSize(): Boolean {
		val weights = storageService.getWeights()?.trainingWeights?.get(0)?.weights ?: return false
		val firstKey = weights.keys.first()
		val featuresSize = weights[firstKey]?.size ?: return false
		return (featuresSize - _customFeaturesSize - _inputDataSize == FEATURE_SIZE)
	}

	private fun initialize() {
		if (_customFeaturesSize > 0) {
			lernaService.initCustomFeatureSize(_customFeaturesSize)
		}
		lernaService.start()
	}

	private fun validateEventNumber(eventNumber: Int) {
		if (eventNumber <= LernaService.SUCCESS_INVALID) {
			throw IllegalArgumentException("Invalid event number. Value should be greater than ${LernaService.SUCCESS_INVALID}.")
		}
		val weights = storageService.getWeights()?.trainingWeights?.get(0)?.weights
		if (weights != null
			&& eventNumber >= weights.keys.size
		) {
			throw IllegalArgumentException("Invalid event number. Value should be within weights range.")
		}
	}

	private fun runFL() {
		flWorker.startFL(_token, uniqueID)
	}

	private fun runCleanUp() {
		ContextRunner().runBlocking(_context, ::runCleanUpWithContext)
	}

	private suspend fun runCleanUpWithContext() {
		FileUtil().cleanUp(storageService.getSessionID())
	}
}
