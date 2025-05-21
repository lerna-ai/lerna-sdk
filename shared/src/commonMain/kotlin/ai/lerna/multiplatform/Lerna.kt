package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.UserID
import ai.lerna.multiplatform.service.ConfigService
import ai.lerna.multiplatform.service.EncryptionService
import ai.lerna.multiplatform.service.FileUtil
import ai.lerna.multiplatform.service.LernaService
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.service.WeightsManager
import ai.lerna.multiplatform.service.actionML.ActionMLService
import ai.lerna.multiplatform.service.actionML.converter.RecommendationConverter
import ai.lerna.multiplatform.service.actionML.dto.QueryRules
import ai.lerna.multiplatform.service.actionML.dto.Result
import com.soywiz.klock.DateTime
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class Lerna(context: KMMContext, token: String) {
	private val _context = context
	private var customFeaturesSize = 0
	private var inputDataSize = 0
	private var _token = token
	private val uniqueID = UserID().getUniqueId(_context).toLong()
	private val storageService = StorageImpl(_context)
	private val weightsManager = WeightsManager(token, uniqueID)
	private val flWorker = FLWorkerInterface(_context)
	private lateinit var lernaService: LernaService
	private lateinit var actionMLService: ActionMLService
	private lateinit var encryptionService: EncryptionService
	private var actionMLDisabled = true
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
				disabled = !ConfigService(_context, _token, uniqueID).updateConfig()
			}
			lernaService = LernaService(_context, _token, uniqueID)
			if (disabled) {
				Napier.d("The Lerna token cannot be validated, Library disabled", null, "Lerna")
			}
			else {
				storageService.getEncryptionKey()?.let {encryptionService = EncryptionService(it)}
				storageService.getCustomFeaturesSize().let { customFeaturesSize = it }
				storageService.getInputDataSize().let {
					inputDataSize = it
					lernaService.initInputSize(it)
				}
				storageService.getCleanupThreshold().let { cleanupThreshold = it.toLong() }
				actionMLService = ActionMLService(storageService.getFLServer(), _token)
				storageService.getActionMLEnabled().let { actionMLDisabled = !it }
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

	fun stop(captureFailure: Boolean = true) {
		if (!started) {
			Napier.d("Stop library error. Lerna already stopped!", null, "Lerna")
			return
		}
		if (!disabled) {
			lernaService.stop(captureFailure)
			started = false
		}
	}

	fun setUserIdentifier(userID: String) {
		if (!disabled) {
			storageService.putUserIdentifier(userID)
		}
	}

	fun captureEvent(modelName: String, positionID: String, successVal: String, elementID: String = "", eventTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), captureOnce: Boolean = true) {
		if (!disabled) {
			lernaService.captureEvent(modelName, positionID, successVal, elementID, captureOnce)
			if (!actionMLDisabled && !storageService.getActionMLEncryption()) {
				runBlocking {
					val eventDateTime = DateTime(eventTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
					val resp = actionMLService.sendEvent(storageService.getUserIdentifier() ?: uniqueID.toString(), modelName, successVal, elementID, eventDateTime)
					Napier.d("Submit event ${resp.comment}", null, "Lerna")
				}
			}
		}
	}

	fun submitRecommendationEvent(modelName: String, successVal: String, elementID: String, eventTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())) {
		if (!disabled && !actionMLDisabled) {
			if (!storageService.getActionMLEncryption()) {
				Napier.w("Recommendation encryption is disabled, use captureEvent() method to submit events", null, "Lerna")
				return
			}
			runBlocking {
				val eventDateTime = DateTime(eventTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
				val resp = actionMLService.sendEvent(storageService.getUserIdentifier() ?: uniqueID.toString(), modelName, successVal, encryptionService.encrypt(elementID), eventDateTime)
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
		if (values.size != inputDataSize) {
			Napier.d("Add input data error, Incorrect input data size", null, "Lerna")
			return
		}
		if (itemID.contains("|")) {
			Napier.d(
				"Add input data error, itemID should not contains vertical bar character (|)",
				null,
				"Lerna"
			)
			return
		}
		lernaService.addInputData(itemID, values, positionID, disabled)
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

	fun refresh(modelName: String, captureFailure: Boolean = true) {
		if (!disabled) {
			lernaService.refresh(modelName, captureFailure)
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
		if (disabled || actionMLDisabled) {
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
		if (disabled || actionMLDisabled) {
			return data
		}
		if (!storageService.getActionMLEncryption()) {
			Napier.i("Recommendation encryption is disabled.", null, "Lerna")
			return data
		}
		return encryptionService.decrypt(data)
	}

	fun manualInference(positionID: String, elementID: String, features: FloatArray) {
		lernaService.manualInference(positionID, elementID, features)
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
