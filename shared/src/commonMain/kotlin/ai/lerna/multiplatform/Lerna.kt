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

class Lerna(context: KMMContext, token: String, customFeaturesSize: Int = 0) {
	private val _context = context
	private val _customFeaturesSize = customFeaturesSize
	private var _inputDataSize = 0
	private var _token = token
	private val uniqueID = UserID().getUniqueId(_context).toLong()
	private val storageService = StorageImpl(_context)
	private val weightsManager = WeightsManager(token, uniqueID)
	private val flWorker = FLWorkerInterface(_context)
	private val lernaService = LernaService(_context, _token, uniqueID)

	init {
		Napier.base(DebugAntilog())
		Napier.d("Initialize library", null, "Lerna")
		weightsManager.setupStorage(storageService)
		runBlocking {
			ConfigService(_token, uniqueID).requestConfig()?.let { response ->
				response.mpcServerUri?.let { storageService.putMPCServer(it) }
				response.flServerUri?.let { storageService.putFLServer(it) }
				response.uploadPrefix?.let { storageService.putUploadPrefix(it) }
				response.logSensorData.let { storageService.putUploadDataEnabled(it) }
				response.abTest.let {
					if(storageService.getABTestPer()!=it) {
						storageService.putABTest(Random.nextFloat() < it)
						storageService.putABTestPer(it)
						Napier.d("I am choosing ${if (storageService.getABTest()) "" else "non"} randomly ABTest", null, "Lerna")
					}
				}
			}
		}
		runBlocking {
			weightsManager.updateWeights()
		}
		runFL()
	}

	fun setInputSize(size: Int) {
		if (_inputDataSize == size) {
			return
		}
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
		runCleanUp()
		initialize()
	}

	fun stop() {
		lernaService.stop()
	}

	fun setUserIdentifier(userID: String) {
		storageService.putUserIdentifier(userID)
	}


	fun captureEvent(modelName:String, positionID: String, successVal: String) {
		lernaService.captureEvent(modelName, positionID, successVal)
	}

	fun updateFeature(values: FloatArray) {
		if (values.size != _customFeaturesSize) {
			throw IllegalArgumentException("Incorrect feature size")
		}
		lernaService.updateFeatures(values)
	}

	fun addInputData(itemID: String, values: FloatArray, positionID: String) {
		if (values.size != _inputDataSize) {
			throw IllegalArgumentException("Incorrect input data size")
		}
		lernaService.addInputData(itemID, values, positionID)
	}

	fun triggerInference(modelName: String, positionID: String? = null, predictionClass: String? = null): String? {
		return lernaService.triggerInference(modelName, positionID, predictionClass)
	}

	fun setAutoInference(modelName: String, setting: String) {
		lernaService.setAutoInference(modelName, setting)
	}

	fun enableUserDataUpload(enable: Boolean) {
		storageService.putUploadDataEnabled(enable)
	}

	fun refresh(modelName:String) {
		lernaService.refresh(modelName)
	}

	private fun initialize() {
		if (_customFeaturesSize > 0) {
			lernaService.initCustomFeatureSize(_customFeaturesSize)
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
		FileUtil().cleanUp(storageService.getSessionID())
	}
}
