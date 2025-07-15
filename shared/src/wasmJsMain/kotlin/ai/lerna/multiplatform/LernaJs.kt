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
import ai.lerna.multiplatform.service.actionML.dto.QueryRules
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import korlibs.io.async.runBlockingNoJs
import korlibs.time.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise
import kotlin.js.Promise
import kotlin.random.Random


private var _context:KMMContext = KMMContext()
private var customFeaturesSize = 0
private var inputDataSize = 0
private lateinit var _token:String //= token
private var uniqueID:Long = -1// = UserID().getUniqueId(_context).toLong()
private lateinit var storageService:StorageImpl// = StorageImpl(_context)
private lateinit var weightsManager:WeightsManager// = WeightsManager(token, uniqueID)
private lateinit var flWorker:FLWorkerInterface // = FLWorkerInterface(_context)
private lateinit var lernaService:LernaService// = LernaService(_context, _token, uniqueID)
private lateinit var actionMLService: ActionMLService
private lateinit var encryptionService: EncryptionService
private var disabled = false
private var started = false
private var cleanupThreshold = 50000000L

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport
fun init(token:String) = GlobalScope.promise {
    _token = token
    uniqueID = UserID().getUniqueId(_context).toLong()
    storageService = StorageImpl(_context)
    weightsManager = WeightsManager(token, uniqueID)
    flWorker = FLWorkerInterface(_context)
    lernaService = LernaService(_context, _token, uniqueID)
    try {
        Napier.base(DebugAntilog())
        Napier.d("Initialize library", null, "Lerna")
        disabled = false //just to be safe...
        GlobalScope.promise {
            disabled = !ConfigService(_context, _token, uniqueID).updateConfig()
        }.then {
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
                weightsManager.setupStorage(storageService)
                GlobalScope.promise {
                    weightsManager.updateWeights()
                }.then {
                    runFL() as JsAny
                }
            } as JsAny
        }
    } catch (e: Exception) {
        Napier.d("The Lerna token cannot be validated, Library disabled with error ${e.message}", e, "Lerna")
        disabled = true
    }
}


@OptIn(ExperimentalJsExport::class)
@JsExport
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

@OptIn(ExperimentalJsExport::class)
@JsExport
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

@OptIn(ExperimentalJsExport::class)
@JsExport
fun setUserIdentifier(userID: String) {
    if (!disabled) {
        storageService.putUserIdentifier(userID)
    }
}


@OptIn(ExperimentalJsExport::class)
@JsExport
fun setAutoInference(modelName: String, setting: String) {
    if (!disabled) {
        lernaService.setAutoInference(modelName, setting)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun enableUserDataUpload(enable: Boolean) {
    if (!disabled) {
        storageService.putUploadDataEnabled(enable)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun refresh(modelName: String) {
    if (!disabled) {
        lernaService.refresh(modelName)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun triggerInference(modelName: String, positionID: String?, predictionClass: String?, numElements: Int?): Promise<JsAny?> {
    val numEl = numElements ?: 1
    return CoroutineScope(Dispatchers.Default).promise {
        lernaService.triggerInference(modelName, positionID, predictionClass, disabled, numEl)?.toJsString()
    }
}

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport
fun captureEvent(modelName: String, positionID: String, successVal: String, elementID: String?) = GlobalScope.promise {
    val elID = if (elementID.isNullOrBlank()){
        ""
    } else {
        elementID
    }
    if (!disabled) {
        lernaService.captureEvent(modelName, positionID, successVal, elID) //maybe add the captureOnce flag as well
        //ToDo: Enable/disabled functionality
        if (!storageService.getActionMLEncryption()) {
            CoroutineScope(Dispatchers.Default).promise {
                val resp = actionMLService.sendEvent(
                    storageService.getUserIdentifier() ?: uniqueID.toString(),
                    modelName,
                    successVal,
                    elID,
                    DateTime.now()
                )
                Napier.d("Submit event ${resp.comment}", null, "Lerna")
            }
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun submitRecommendationEvent(modelName: String, successVal: String, elementID: String) {
    if (!disabled) {
        if (!storageService.getActionMLEncryption()) {
            Napier.w("Recommendation encryption is disabled, use captureEvent() method to submit events", null, "Lerna")
            return
        }
        CoroutineScope(Dispatchers.Default).promise {
            val resp = actionMLService.sendEvent(storageService.getUserIdentifier() ?: uniqueID.toString(), modelName, successVal, encryptionService.encrypt(elementID), DateTime.now())
            Napier.d("Submit encrypted event ${resp.comment}", null, "Lerna")
        }
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun updateFeature(values: JsArray<JsAny>) {
    if (!disabled) {
        if (values.length != customFeaturesSize) {
            Napier.d("Update feature error, Incorrect feature size", null, "Lerna")
            return
        }
        val floatArray = FloatArray(values.length)
        for (i in (0..<values.length)){
            floatArray[i] =  values[i].toString().toFloat()
        }
        lernaService.updateFeatures(floatArray)
    }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
fun addInputData(itemID: String, values: JsArray<JsAny>, positionID: String) {
    Napier.d("addInputData($itemID, ${values}, $positionID)", null, "Lerna")
    if (!disabled) {
        if (values.length != inputDataSize) {
            Napier.d("Add input data error, Incorrect input data size", null, "Lerna")
            return
        }
        if (itemID.contains("|")) {
            Napier.d("Add input data error, itemID should not contains vertical bar character (|)", null, "Lerna")
            return
        }

        val floatArray = FloatArray(values.length)
        for (i in (0..<values.length)){
            floatArray[i] = values[i].toString().toFloat()
        }

        lernaService.addInputData(itemID, floatArray, positionID, disabled)
    }
}

@OptIn(ExperimentalJsExport::class, DelicateCoroutinesApi::class)
@JsExport
fun getRecommendations(modelName: String, number: Int?): JsAny = GlobalScope.promise {
    return@promise getRecommendations(
        modelName = modelName,
        number = number,
        blacklistItems = null,
        rules = null
    ).toJsString()
}

@OptIn(ExperimentalJsExport::class)
@JsExport
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
    Napier.d("runFL()", null, "Lerna")
    flWorker.startFL(_token, uniqueID)
}

private fun runCleanUp() {
    ContextRunner().runBlocking(_context, ::runCleanUpWithContext)
}

private suspend fun runCleanUpWithContext() {
    FileUtil().cleanUp(storageService.getSessionID(), cleanupThreshold)
}

private suspend fun getRecommendations(modelName: String, number: Int?, blacklistItems: List<String>?, rules: List<QueryRules>?): String {
    if (disabled) {
        return "{}"
    }
    return actionMLService.getUserItemsAsJsonText(
        engineID = modelName,
        num = number,
        user = storageService.getUserIdentifier() ?: uniqueID.toString(),
        blacklistItems = blacklistItems,
        rules = rules)
}
