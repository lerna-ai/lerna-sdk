package ai.lerna.multiplatform

import ai.lerna.multiplatform.utils.addBoxed
import ai.lerna.multiplatform.utils.energy
import ai.lerna.multiplatform.utils.fft
import ai.lerna.multiplatform.utils.getAverageResultant
import ai.lerna.multiplatform.utils.getHistoricCalculations
import ai.lerna.multiplatform.utils.getSMA
import ai.lerna.multiplatform.utils.iqr
import ai.lerna.multiplatform.utils.mad
import ai.lerna.multiplatform.utils.numPeaks
import ai.lerna.multiplatform.utils.range
import ai.lerna.multiplatform.utils.skewnessKurtosis
import ai.lerna.multiplatform.utils.std

actual class ModelData actual constructor(_historySize: Int) {
	internal actual val historySize = _historySize

	//private var proximity: Float = 0F
	private var linAcceleration = arrayOf(0F, 0F, 0F)
	private var gyroscope = arrayOf(0F, 0F, 0F)
	private var magneticField = arrayOf(0F, 0F, 0F)
	private var magneticFieldMean = arrayOf(0F, 0F, 0F)
	private var magneticFieldStd = arrayOf(0F, 0F, 0F)
	internal actual var morning: Float = 0F
	internal actual var afternoon: Float = 0F
	internal actual var evening: Float = 0F
	internal actual var night: Float = 0F
	internal actual var weekday: Float = 0F
	internal actual var weekend: Float = 0F
	private var wiredPhones: Float = 0F
	private var linAccelerationHistoryX: ArrayDeque<Float> = ArrayDeque()
	private var linAccelerationHistoryY: ArrayDeque<Float> = ArrayDeque()
	private var linAccelerationHistoryZ: ArrayDeque<Float> = ArrayDeque()
	private var gyroscopeHistoryX: ArrayDeque<Float> = ArrayDeque()
	private var gyroscopeHistoryY: ArrayDeque<Float> = ArrayDeque()
	private var gyroscopeHistoryZ: ArrayDeque<Float> = ArrayDeque()
	private var magneticFieldHistoryX: ArrayDeque<Float> = ArrayDeque()
	private var magneticFieldHistoryY: ArrayDeque<Float> = ArrayDeque()
	private var magneticFieldHistoryZ: ArrayDeque<Float> = ArrayDeque()
	private var orientation: Float = 0F
	private var batteryPlugged: Float = 0F
	private var audioVolume: Float = 0F
	private var audioBtScoOn: Float = 0F
	private var audioMusicActive: Float = 0F
	private var audioSpeakerOn: Float = 0F
	private var audioHeadsetOn: Float = 0F
	private var ambientLight: Float = 0F
	private var ambientLightHistory: ArrayDeque<Float> = ArrayDeque()
	private var ambientLightMean: Float = 0F
	private var ambientLightStd: Float = 0F
	private var wifiConnected: Float = 0F
	private var customFeaturesArray: FloatArray = FloatArray(0)

	private var modelDataHistory: ArrayDeque<String> = ArrayDeque(0)

	private var linAccelerationHistoryXFft: ArrayDeque<Float> = ArrayDeque()
	private var linAccelerationHistoryYFft: ArrayDeque<Float> = ArrayDeque()
	private var linAccelerationHistoryZFft: ArrayDeque<Float> = ArrayDeque()

	private var gyroscopeHistoryXFft: ArrayDeque<Float> = ArrayDeque()
	private var gyroscopeHistoryYFft: ArrayDeque<Float> = ArrayDeque()
	private var gyroscopeHistoryZFft: ArrayDeque<Float> = ArrayDeque()

	internal actual fun setupCustomFeatureSize(customFeatureSize: Int) {
		if (customFeaturesArray.size != customFeatureSize && customFeatureSize != 0) {
			customFeaturesArray = FloatArray(customFeatureSize)
		}
	}

	internal actual fun updateCustomFeatures(customFeatures: FloatArray) {
		if (customFeaturesArray.size != customFeatures.size) {
			return
		}
		customFeaturesArray = customFeatures
	}

//	internal fun setProximity(proximity: Float) {
//		this.proximity = proximity
//	}

	internal fun setLinAcceleration(accX: Float, accY: Float, accZ: Float) {
		linAcceleration[0] = accX
		linAcceleration[1] = accY
		linAcceleration[2] = accZ
	}

	internal fun setGyroscope(gyrX: Float, gyrY: Float, gyrZ: Float) {
		gyroscope[0] = gyrX
		gyroscope[1] = gyrY
		gyroscope[2] = gyrZ
	}

	internal fun setMagneticField(magX: Float, magY: Float, magZ: Float) {
		magneticField[0] = magX
		magneticField[1] = magY
		magneticField[2] = magZ

	}

	internal fun setLight(light: Float) {
		ambientLight = light
	}

	internal fun setPhones(headphones: Boolean) {
		wiredPhones = if (headphones)
			1F
		else
			0F
	}

	internal fun setOrientation(_orientation: Float) {
		orientation = _orientation
	}

	internal fun setBatteryChargingState(chargingState: Int) {
		batteryPlugged = when (chargingState) {
			1, 2, 4 -> { // BATTERY_PLUGGED_AC, BATTERY_PLUGGED_USB, BATTERY_PLUGGED_WIRELESS
				1F
			}

			else -> {
				0F
			}
		}
	}

	internal fun setAudioActivity(
		volume: Float,
		isBtScoOn: Boolean,
		isMusicActive: Boolean,
		isSpeakerOn: Boolean,
		isHeadsetOn: Boolean
	) {
		audioVolume = volume
		audioBtScoOn = if (isBtScoOn) 1F else 0F
		audioMusicActive = if (isMusicActive) 1F else 0F
		audioSpeakerOn = if (isSpeakerOn) 1F else 0F
		audioHeadsetOn = if (isHeadsetOn) 1F else 0F
	}

	internal fun setWifiConnected(isConnectionUnmetered: Boolean) {
		wifiConnected = if (isConnectionUnmetered) 1F else 0F
	}

	internal actual fun clearHistory() {
		linAccelerationHistoryX.clear()
		linAccelerationHistoryY.clear()
		linAccelerationHistoryZ.clear()

		linAccelerationHistoryXFft.clear()
		linAccelerationHistoryYFft.clear()
		linAccelerationHistoryZFft.clear()

		gyroscopeHistoryX.clear()
		gyroscopeHistoryY.clear()
		gyroscopeHistoryZ.clear()

		gyroscopeHistoryXFft.clear()
		gyroscopeHistoryYFft.clear()
		gyroscopeHistoryZFft.clear()

		magneticFieldHistoryX.clear()
		magneticFieldHistoryY.clear()
		magneticFieldHistoryZ.clear()

		ambientLightHistory.clear()
		modelDataHistory.clear()
	}

	internal actual fun setDateTime(hour: Int, dayOfWeek: Int) {
		when (hour) {
			in 0..11 -> {
				morning = 1F
				afternoon = 0F
				evening = 0F
				night = 0F
			}

			in 12..15 -> {
				morning = 0F
				afternoon = 1F
				evening = 0F
				night = 0F
			}

			in 16..20 -> {
				morning = 0F
				afternoon = 0F
				evening = 1F
				night = 0F
			}

			in 21..23 -> {
				morning = 0F
				afternoon = 0F
				evening = 0F
				night = 1F
			}
		}

		if (dayOfWeek == 5 || // SATURDAY
			dayOfWeek == 6    // SUNDAY
		) {
			weekend = 1F
			weekday = 0F
		} else {
			weekend = 0F
			weekday = 1F
		}
	}

	internal actual fun setHistory(time: String) {
		try {
			linAccelerationHistoryX.addBoxed(linAcceleration[0])
			linAccelerationHistoryY.addBoxed(linAcceleration[1])
			linAccelerationHistoryZ.addBoxed(linAcceleration[2])

			gyroscopeHistoryX.addBoxed(gyroscope[0])
			gyroscopeHistoryY.addBoxed(gyroscope[1])
			gyroscopeHistoryZ.addBoxed(gyroscope[2])

			magneticFieldHistoryX.addBoxed(magneticField[0])
			magneticFieldHistoryY.addBoxed(magneticField[1])
			magneticFieldHistoryZ.addBoxed(magneticField[2])

			if (magneticFieldHistoryX.isNotEmpty()) {
				magneticFieldMean = arrayOf(
					magneticFieldHistoryX.average().toFloat(),
					magneticFieldHistoryY.average().toFloat(),
					magneticFieldHistoryZ.average().toFloat()
				)
				magneticFieldStd = arrayOf(
					magneticFieldHistoryX.std(),
					magneticFieldHistoryY.std(),
					magneticFieldHistoryZ.std()
				)
			}

			ambientLightHistory.addBoxed(ambientLight)
			if (ambientLightHistory.isNotEmpty()) {
				ambientLightMean = ambientLightHistory.average().toFloat()
				ambientLightStd = ambientLightHistory.std()
			}

			linAccelerationHistoryXFft = linAccelerationHistoryX.fft()
			linAccelerationHistoryYFft = linAccelerationHistoryY.fft()
			linAccelerationHistoryZFft = linAccelerationHistoryZ.fft()
			gyroscopeHistoryXFft = gyroscopeHistoryX.fft()
			gyroscopeHistoryYFft = gyroscopeHistoryY.fft()
			gyroscopeHistoryZFft = gyroscopeHistoryZ.fft()

			modelDataHistory.addBoxed("$time,${toCsv()}", historySize)
		} catch (_: Exception) {
		}
	}

	internal actual fun historyToCsv(): String {
		return modelDataHistory
			.takeLast(historySize)
			.joinToString(separator = "\n", postfix = "\n") { it }
	}

	internal actual fun historyToCsv(sessionID: Int, successValue: String): String {
		return modelDataHistory
			.takeLast(historySize)
			.joinToString(
				prefix = "$sessionID,",
				separator = ",$successValue\n$sessionID,",
				postfix = ",$successValue\n"
			) { it }
	}

	internal actual fun toCsv(): String {
		return toArray().joinToString(separator = ",") { it.toString() }
	}

	internal actual fun toArray(): Array<Float> {
		val doubleArray: MutableList<Float> = ArrayList()
		//doubleArray.add(proximity)

		try {
			val historyDeques = listOf(
				linAccelerationHistoryX, linAccelerationHistoryY, linAccelerationHistoryZ,
				gyroscopeHistoryX, gyroscopeHistoryY, gyroscopeHistoryZ,
				linAccelerationHistoryXFft, linAccelerationHistoryYFft, linAccelerationHistoryZFft,
				gyroscopeHistoryXFft, gyroscopeHistoryYFft, gyroscopeHistoryZFft
			)

			// NEW: Total Num of features for Accelerometer + Gyroscope now = 12*12 + 6 = 150
			for (deque in historyDeques) {
				doubleArray.addAll(getHistoricCalculations(deque))
			}

			doubleArray.add(getSMA(linAccelerationHistoryX, linAccelerationHistoryY, linAccelerationHistoryZ))
			doubleArray.add(getAverageResultant(linAccelerationHistoryX, linAccelerationHistoryY, linAccelerationHistoryZ))
			doubleArray.add(getSMA(linAccelerationHistoryXFft, linAccelerationHistoryYFft, linAccelerationHistoryZFft))

			doubleArray.add(getSMA(gyroscopeHistoryX, gyroscopeHistoryY, gyroscopeHistoryZ))
			doubleArray.add(getAverageResultant(gyroscopeHistoryX, gyroscopeHistoryY, gyroscopeHistoryZ))
			doubleArray.add(getSMA(gyroscopeHistoryXFft, gyroscopeHistoryYFft, gyroscopeHistoryZFft))
		} catch (_: Exception) {
			// If any calculation failed, fill the rest values with zero
			for (i in doubleArray.size..149) doubleArray.add(0.0f)
		}


		doubleArray.add(magneticField[0])
		doubleArray.add(magneticField[1])
		doubleArray.add(magneticField[2])
		doubleArray.add(magneticFieldMean[0])
		doubleArray.add(magneticFieldMean[1])
		doubleArray.add(magneticFieldMean[2])
		doubleArray.add(magneticFieldStd[0])
		doubleArray.add(magneticFieldStd[1])
		doubleArray.add(magneticFieldStd[2])

		doubleArray.add(morning)
		doubleArray.add(afternoon)
		doubleArray.add(evening)
		doubleArray.add(night)
		doubleArray.add(weekday)
		doubleArray.add(weekend)
		doubleArray.add(wiredPhones)
		doubleArray.add(orientation)
		doubleArray.add(batteryPlugged)
		doubleArray.add(audioVolume)
		doubleArray.add(audioBtScoOn)
		doubleArray.add(audioMusicActive)
		doubleArray.add(audioSpeakerOn)
		doubleArray.add(audioHeadsetOn)
		doubleArray.add(ambientLight)
		doubleArray.add(ambientLightMean)
		doubleArray.add(ambientLightStd)
		doubleArray.add(wifiConnected)
		doubleArray.addAll(customFeaturesArray.map { it })
		return doubleArray.toTypedArray()
	}

	internal actual fun historyToArray(): Array<FloatArray> {
		return modelDataHistory
			.takeLast(historySize)
			.map { line ->
				line.split(",")
					.filter { !it.contains("_") }
					.map { it.toFloat() }.toFloatArray()
			}
			.toTypedArray()
	}

	internal actual fun isHistoryNonEmpty(): Boolean {
		return modelDataHistory.isNotEmpty()
	}

	// Help methods for test purposes
	internal fun getStd(array: ArrayDeque<Float>): Float = array.std()
	internal fun getMad(array: ArrayDeque<Float>): Pair<Float, Float> = array.mad()
	internal fun getEnergy(array: ArrayDeque<Float>): Float = array.energy()
	internal fun getRange(array: ArrayDeque<Float>): Float = array.range()
	internal fun getIqr(array: ArrayDeque<Float>): Float = array.iqr()
	internal fun getSkewnessKurtosis(array: ArrayDeque<Float>): Pair<Float, Float> = array.skewnessKurtosis()
	internal fun getFft(array: ArrayDeque<Float>): ArrayDeque<Float> = array.fft()
	internal fun getNumPeaks(array: ArrayDeque<Float>): Int = array.numPeaks()
}
