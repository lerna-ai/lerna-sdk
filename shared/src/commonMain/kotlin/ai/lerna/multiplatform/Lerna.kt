package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.UserID
import ai.lerna.multiplatform.service.ConfigService
import ai.lerna.multiplatform.service.FileUtil
import ai.lerna.multiplatform.service.LernaService
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.service.WeightsManager
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

class Lerna(context: KMMContext, token: String) {
	private val _context = context
	private var customFeaturesSize = 0
	private var inputDataSize = 0
	private var _token = token
	private val uniqueID = UserID().getUniqueId(_context).toLong()
	private val storageService = StorageImpl(_context)
	private val weightsManager = WeightsManager(token, uniqueID)
	private val flWorker = FLWorkerInterface(_context)
	private val lernaService = LernaService(_context, _token, uniqueID)
	private var disabled = false
	private var started = false
	private var cleanupThreshold = 50000000L

	init {
		try {
			Napier.base(DebugAntilog())
			Napier.d("Initialize library", null, "Lerna")
			disabled = false //just to be safe...
			runBlocking {
				ConfigService(_token, uniqueID).requestConfig()?.let { response ->
					response.mpcServerUri?.let { storageService.putMPCServer(it) }
					response.flServerUri?.let { storageService.putFLServer(it) }
					response.uploadPrefix?.let { storageService.putUploadPrefix(it) }
					response.logSensorData.let { storageService.putUploadDataEnabled(it) }
					response.abTest.let {
						if (storageService.getABTestPer() != it) {
							storageService.putABTest(Random.nextFloat() < it)
							storageService.putABTestPer(it)
							Napier.d(
								"I am choosing ${if (storageService.getABTest()) "" else "non"} randomly ABTest",
								null,
								"Lerna"
							)
						}
					}
					response.customFeaturesSize.let { customFeaturesSize = it }
					response.inputDataSize.let {
						inputDataSize = it
						lernaService.initInputSize(it)
					}
					response.sensorInitialDelay.let { storageService.putSensorInitialDelay(it) }
					response.trainingDataThreshold.let { storageService.putTrainingDataThreshold(it) }
					response.trainingSessionsThreshold.let { storageService.putTrainingSessionsThreshold(it) }
					response.cleanupThreshold.let { cleanupThreshold = it.toLong() }
				} ?: run {
					Napier.d("The Lerna token cannot be validated, Library disabled", null, "Lerna")
					disabled = true
				}
			}
			if (!disabled) {
				weightsManager.setupStorage(storageService)
				runBlocking {
					weightsManager.updateWeights()
				}
				runFL()
			}
		} catch (e: Exception) {
			Napier.d("The Lerna token cannot be validated, Library disabled with error ${e.message}", e, "Lerna")
			disabled = true
		}
	}

	fun start() {
		if (started) {
			Napier.d("Start library error. Lerna already started!", null, "Lerna")
			return
		}
		if (!disabled) {
			runCleanUp()
			initialize()
			started = true
		}
	}

	fun stop() {
		if (!started) {
			Napier.d("Stop library error. Lerna already stopped!", null, "Lerna")
			return
		}
		if (!disabled) {
			lernaService.stop()
			started = false
		}
	}

	fun setUserIdentifier(userID: String) {
		if (!disabled) {
			storageService.putUserIdentifier(userID)
		}
	}


	fun captureEvent(modelName: String, positionID: String, successVal: String) {
		if (!disabled) {
			lernaService.captureEvent(modelName, positionID, successVal)
		}
	}

	fun updateFeature(values: FloatArray) {
		if (!disabled) {
			if (values.size != customFeaturesSize) {
				Napier.d("Update feature error, Incorrect feature size", null, "Lerna")
				return
			}
			lernaService.updateFeatures(values)
		}
	}

	fun addInputData(itemID: String, values: FloatArray, positionID: String) {
		if (!disabled) {
			if (values.size != inputDataSize) {
				Napier.d("Add input data error, Incorrect input data size", null, "Lerna")
				return
			}
			lernaService.addInputData(itemID, values, positionID, disabled)
		}
	}

	fun triggerInference(modelName: String, positionID: String? = null, predictionClass: String? = null): String? {
		return lernaService.triggerInference(modelName, positionID, predictionClass, disabled)
	}

	fun triggerInference(inputData: Map<String, FloatArray>, modelName: String, positionID: String, predictionClass: String? = null): String? {
		lernaService.clearInputData(positionID)
		inputData.forEach { (itemID, values) -> addInputData(itemID, values, positionID) }
		return lernaService.triggerInference(modelName, positionID, predictionClass, disabled)
	}

	fun setAutoInference(modelName: String, setting: String) {
		if (!disabled) {
			lernaService.setAutoInference(modelName, setting)
		}
	}

	fun enableUserDataUpload(enable: Boolean) {
		if (!disabled) {
			storageService.putUploadDataEnabled(enable)
		}
	}

	fun refresh(modelName: String) {
		if (!disabled) {
			lernaService.refresh(modelName)
		}
	}

	private fun initialize() {
		if (customFeaturesSize > 0) {
			lernaService.initCustomFeatureSize(customFeaturesSize)
		}
		lernaService.start()
	}

	private fun runFL() {
		flWorker.startFL(_token, uniqueID)
	}

	private fun runCleanUp() {
		ContextRunner().runBlocking(_context, ::runCleanUpWithContext)
	}

	private suspend fun runCleanUpWithContext() {
		FileUtil().cleanUp(storageService.getSessionID(), cleanupThreshold)
	}
}
