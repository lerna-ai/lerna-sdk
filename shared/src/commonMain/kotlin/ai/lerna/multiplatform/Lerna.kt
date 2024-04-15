package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.UserID
import ai.lerna.multiplatform.service.ConfigService
import ai.lerna.multiplatform.service.EncryptionService
import ai.lerna.multiplatform.service.FileUtil
import ai.lerna.multiplatform.service.LernaService
import ai.lerna.multiplatform.service.MpcService
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.service.WeightsManager
import ai.lerna.multiplatform.service.actionML.ActionMLService
import ai.lerna.multiplatform.service.actionML.converter.RecommendationConverter
import ai.lerna.multiplatform.service.actionML.dto.QueryRules
import ai.lerna.multiplatform.service.actionML.dto.Result
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
	private lateinit var actionMLService: ActionMLService
	private lateinit var encryptionService: EncryptionService
	private var disabled = false
	private var started = false
	private var cleanupThreshold = 50000000L
	private var recommendationConverter: RecommendationConverter? = null

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
					response.uploadSensorData.let { storageService.putUploadDataEnabled(it) }
					response.logSensorData.let { storageService.putLog(it) }
					response.abTest.let {
						if (storageService.getABTestPer() != it) {
							storageService.putABTest(Random.nextFloat() < it)
							storageService.putABTestPer(it)
							Napier.d(
								"I am choosing ${if (storageService.getABTest()) "" else "non "}randomly ABTest",
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
					response.trainingSessionsThreshold.let { storageService.putTrainingSessionsThreshold(it) }
					response.cleanupThreshold.let { cleanupThreshold = it.toLong() }
					response.actionMLEncryption.let {
						storageService.putActionMLEncryption(it)
						if (it) {
							MpcService(storageService.getMPCServer(), token).getEncryptionKey().let { encryption ->
								encryption.key?.let { key ->
									storageService.putEncryptionKey(key)
									encryptionService = EncryptionService(key)
								}
							}
						}
					}
				} ?: run {
					Napier.d("The Lerna token cannot be validated, Library disabled", null, "Lerna")
					disabled = true
				}
			}
			if (!disabled) {
				actionMLService = ActionMLService(storageService.getFLServer(), _token)
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

	fun setupRecommendationConverter(_recommendationConverter: RecommendationConverter) {
		recommendationConverter = _recommendationConverter
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

	fun captureEvent(modelName: String, positionID: String, successVal: String, elementID: String = "") {
		if (!disabled) {
			lernaService.captureEvent(modelName, positionID, successVal, elementID)
			//ToDo: Enable/disabled functionality
			if (!storageService.getActionMLEncryption()) {
				runBlocking {
					val resp = actionMLService.sendEvent(storageService.getUserIdentifier() ?: uniqueID.toString(), modelName, successVal, elementID)
					Napier.d("Submit event ${resp.comment}", null, "Lerna")
				}
			}
		}
	}

	fun submitRecommendationEvent(modelName: String, successVal: String, elementID: String) {
		if (!disabled) {
			if (!storageService.getActionMLEncryption()) {
				Napier.w("Recommendation encryption is disabled, use captureEvent() method to submit events", null, "Lerna")
				return
			}
			runBlocking {
				val resp = actionMLService.sendEvent(storageService.getUserIdentifier() ?: uniqueID.toString(), modelName, successVal, encryptionService.encrypt(elementID))
				Napier.d("Submit encrypted event ${resp.comment}", null, "Lerna")
			}
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
			if (itemID.contains("|")) {
				Napier.d("Add input data error, itemID should not contains vertical bar character (|)", null, "Lerna")
				return
			}
			lernaService.addInputData(itemID, values, positionID, disabled)
		}
	}

	fun triggerInference(modelName: String, positionID: String? = null, predictionClass: String? = null, numElements: Int = 1): String? {
		return lernaService.triggerInference(modelName, positionID, predictionClass, disabled, numElements)
	}

	fun triggerInference(inputData: Map<String, FloatArray>, modelName: String, positionID: String, predictionClass: String? = null, numElements: Int = 1): String? {
		lernaService.clearInputData(positionID)
		inputData.forEach { (itemID, values) -> addInputData(itemID, values, positionID) }
		return lernaService.triggerInference(modelName, positionID, predictionClass, disabled, numElements)
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

	fun getRecommendations(modelName: String): List<Any> {
		return getRecommendations(
			modelName = modelName,
			number = null,
			blacklistItems = null,
			rules = null
		)
	}

	fun getRecommendations(modelName: String, number: Int?): List<Any> {
		return getRecommendations(
			modelName = modelName,
			number = number,
			blacklistItems = null,
			rules = null
		)
	}

	fun getRecommendations(modelName: String, number: Int?, blacklistItems: List<String>?, rules: List<QueryRules>?): List<Any> {
		if (disabled) {
			return listOf()
		}
		var response: List<Result> = mutableListOf()
		runBlocking {
			actionMLService.getUserItems(
				engineID = modelName,
				num = number,
				user = storageService.getUserIdentifier() ?: uniqueID.toString(),
				blacklistItems = blacklistItems,
				rules = rules
			).result.let {
				if (!it.isNullOrEmpty()) {
					response = it
				}
			}
		}
		if (recommendationConverter != null) {
			return response.map { recommendationConverter!!.convert(it) }
		}
		return response
	}

	fun decryptRecommendationData(data: String): String {
		if (disabled) {
			return data
		}
		if (!storageService.getActionMLEncryption()) {
			Napier.i("Recommendation encryption is disabled.", null, "Lerna")
			return data
		}
		return encryptionService.decrypt(data)
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
