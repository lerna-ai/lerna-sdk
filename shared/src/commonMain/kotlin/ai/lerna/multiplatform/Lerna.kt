package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.KMMPreference
import ai.lerna.multiplatform.config.UserID
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.service.WeightsManager
import org.jetbrains.kotlinx.multik.ndarray.operations.Log

class Lerna(context: KMMContext, token: String, customFeaturesSize: Int = 0) {
	private val _context = context
	private val _customFeaturesSize = customFeaturesSize
	private var _token = token
	private val uniqueID = UserID().getUniqueId(_context).toLong()
	private val storageService = StorageImpl(_context)
	private val weightsManager = WeightsManager(token, uniqueID)
	private val sharedPref: KMMPreference = KMMPreference(_context)
	private val flWorker = FLWorkerInterface(_context)

	internal companion object {
		const val FEATURE_SIZE_WITHOUT_EXTRA = 57 // Lerna features plus x0
		const val FEATURE_SIZE_WITH_EXTRA = 68    // Lerna features with bluetooth features plus x0
	}

	init {
	}

	fun start() {
		if (checkWeightSize()) {
			initialize()
			runFL()
			runCleanUp()
		}
	}

	fun stop() {
//		if (_foregroundServiceEnabled) {
//			val broadcastIntent = Intent(_context, ServiceRestarter::class.java)
//			broadcastIntent.action = "restartLernaService"
//			_context.sendBroadcast(broadcastIntent)
//		}
//		else {
//			_context.sendBroadcast(Intent(ACTION_APP_STOP))
//		}
	}

	fun setUserIdentifier(userID: String) {
		storageService.putUserIdentifier(userID)
	}

	fun captureEvent(event: LernaEvent = LernaEvent.SUCCESS) {
		captureEvent(event.value)
	}

	fun captureEvent(eventNumber: Int) {
		validateEventNumber(eventNumber)
//		lernaServiceIntent = initLernaServiceIntent(_context)
//		lernaServiceIntent?.putExtra("successValue", eventNumber)
//		if (_foregroundServiceEnabled) {
//			val x = _context.startForegroundService(lernaServiceIntent)
//			Log.d("Lerna", "LernaService restarted to capture event $x")
//		} else {
//			val x = _context.startService(lernaServiceIntent)
//			Log.d("Lerna", "LernaForegroundService restarted to capture event $x")
//		}
	}

	fun updateFeature(values: FloatArray) {
		if (values.size != _customFeaturesSize) {
			throw IllegalArgumentException("Incorrect feature size")
		}
//		val updateIntent = Intent(ACTION_UPDATE_FEATURE)
//		updateIntent.putExtra("payload", values);
//		_context.sendBroadcast(updateIntent)
	}

	private fun checkWeightSize(): Boolean {
		val weights = storageService.getWeights()?.trainingWeights?.get(0)?.weights ?: return false
		val firstKey = weights.keys.first() ?: return false
		val featuresSize = weights[firstKey.toString()]?.size ?: return false
		if (featuresSize - _customFeaturesSize == FEATURE_SIZE_WITHOUT_EXTRA) {
			sharedPref.put("enableBluetoothFeatures", false)
			return true
		}
		else if (featuresSize - _customFeaturesSize == FEATURE_SIZE_WITH_EXTRA) {
			sharedPref.put("enableBluetoothFeatures", true)
			return true // checkSelfPermission(_context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
		}
		return false
	}

	internal fun initialize() {
		// ToDo: Start sensor data collection service
//		lernaServiceIntent = initLernaServiceIntent(_context)
//		if (!isLernaServiceRunning(selectService(_foregroundServiceEnabled))) {
//			if (_foregroundServiceEnabled) {
//				_context.startForegroundService(lernaServiceIntent)
//				Log.d("Lerna", "LernaForegroundService started")
//			} else {
//				_context.startService(lernaServiceIntent)
//				Log.d("Lerna", "LernaService started")
//			}
//		}
	}

	private fun validateEventNumber(eventNumber: Int) {
		if (eventNumber <= 0) {
			throw IllegalArgumentException("Invalid event number. Value should be positive.")
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
		// ToDo: Add clean up worker & interface
		//cleanupWorker.start()
	}
}