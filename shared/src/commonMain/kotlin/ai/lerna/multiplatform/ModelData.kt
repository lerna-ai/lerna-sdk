package ai.lerna.multiplatform

import ai.lerna.multiplatform.utils.DateUtil
import kotlin.math.sqrt

class ModelData(_historySize: Int = 1) {
	private val historicDataSize = 60
	private val historySize = _historySize
	//private var proximity: Float = 0F
	private var linAcceleration = arrayOf(0F, 0F, 0F)
	private var gyroscope = arrayOf(0F, 0F, 0F)
	private var magneticField = arrayOf(0F, 0F, 0F)
	private var linAccelerationMean = arrayOf(0F, 0F, 0F)
	private var gyroscopeMean = arrayOf(0F, 0F, 0F)
	private var magneticFieldMean = arrayOf(0F, 0F, 0F)
	private var linAccelerationStd = arrayOf(0F, 0F, 0F)
	private var gyroscopeStd = arrayOf(0F, 0F, 0F)
	private var magneticFieldStd = arrayOf(0F, 0F, 0F)
	private var morning: Float = 0F
	private var afternoon: Float = 0F
	private var evening: Float = 0F
	private var night: Float = 0F
	private var weekday: Float = 0F
	private var weekend: Float = 0F
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

	internal fun setupCustomFeatureSize(customFeatureSize: Int) {
		if (customFeaturesArray.size != customFeatureSize && customFeatureSize != 0) {
			customFeaturesArray = FloatArray(customFeatureSize)
		}
	}

	internal fun updateCustomFeatures(customFeatures: FloatArray) {
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

	internal fun clearHistory() {
		linAccelerationHistoryX.clear()
		linAccelerationHistoryY.clear()
		linAccelerationHistoryZ.clear()
		gyroscopeHistoryX.clear()
		gyroscopeHistoryY.clear()
		gyroscopeHistoryZ.clear()
		magneticFieldHistoryX.clear()
		magneticFieldHistoryY.clear()
		magneticFieldHistoryZ.clear()
		ambientLightHistory.clear()
		modelDataHistory.clear()
	}

	internal fun setDateTime(hour: Int, dayOfWeek: Int) {
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

	private fun ArrayDeque<Float>.std(): Float {
		val mean = this.average().toFloat()
		return this.fold(0.0) { accumulator, next -> accumulator + (next - mean) * (next - mean) }
			.let { sqrt(it / this.size).toFloat() }
	}

	private fun ArrayDeque<Float>.addBoxed(element: Float) {
		this.add(element)
		if (this.size > historicDataSize) {
			this.removeFirst()
		}
	}

	private fun ArrayDeque<String>.addBoxed(element: String) {
		this.add(element)
		if (this.size > historySize) {
			this.removeFirst()
		}
	}

	internal fun setHistory(time: String = DateUtil().now()) {
		linAccelerationHistoryX.addBoxed(linAcceleration[0])
		linAccelerationHistoryY.addBoxed(linAcceleration[1])
		linAccelerationHistoryZ.addBoxed(linAcceleration[2])
		if (linAccelerationHistoryX.isNotEmpty()) {
			linAccelerationMean = arrayOf(
				linAccelerationHistoryX.average().toFloat(),
				linAccelerationHistoryY.average().toFloat(),
				linAccelerationHistoryZ.average().toFloat()
			)
			linAccelerationStd = arrayOf(
				linAccelerationHistoryX.std(),
				linAccelerationHistoryY.std(),
				linAccelerationHistoryZ.std()
			)
		}

		gyroscopeHistoryX.addBoxed(gyroscope[0])
		gyroscopeHistoryY.addBoxed(gyroscope[1])
		gyroscopeHistoryZ.addBoxed(gyroscope[2])
		if (gyroscopeHistoryX.isNotEmpty()) {
			gyroscopeMean = arrayOf(
				gyroscopeHistoryX.average().toFloat(),
				gyroscopeHistoryY.average().toFloat(),
				gyroscopeHistoryZ.average().toFloat()
			)
			gyroscopeStd = arrayOf(
				gyroscopeHistoryX.std(),
				gyroscopeHistoryY.std(),
				gyroscopeHistoryZ.std()
			)
		}

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
		modelDataHistory.addBoxed("$time,${toCsv()}")
	}

	internal fun historyToCsv(): String {
		return modelDataHistory
			.takeLast(historySize)
			.joinToString(separator = "\n", postfix = "\n") { it }
	}

	internal fun historyToCsv(sessionID: Int, successValue: String): String {
		return modelDataHistory
			.takeLast(historySize)
			.joinToString(
				prefix = "$sessionID,",
				separator = ",$successValue\n$sessionID,",
				postfix = ",$successValue\n") { it }
	}

	internal fun toCsv(): String {
		return toArray().joinToString(separator = ",") { it.toString() }
	}

	internal fun toArray(): Array<Double> {
		val doubleArray: MutableList<Double> = ArrayList()
		//doubleArray.add(proximity.toDouble())
		doubleArray.add(linAcceleration[0].toDouble())
		doubleArray.add(linAcceleration[1].toDouble())
		doubleArray.add(linAcceleration[2].toDouble())
		doubleArray.add(gyroscope[0].toDouble())
		doubleArray.add(gyroscope[1].toDouble())
		doubleArray.add(gyroscope[2].toDouble())
		doubleArray.add(magneticField[0].toDouble())
		doubleArray.add(magneticField[1].toDouble())
		doubleArray.add(magneticField[2].toDouble())
		doubleArray.add(linAccelerationMean[0].toDouble())
		doubleArray.add(linAccelerationMean[1].toDouble())
		doubleArray.add(linAccelerationMean[2].toDouble())
		doubleArray.add(gyroscopeMean[0].toDouble())
		doubleArray.add(gyroscopeMean[1].toDouble())
		doubleArray.add(gyroscopeMean[2].toDouble())
		doubleArray.add(magneticFieldMean[0].toDouble())
		doubleArray.add(magneticFieldMean[1].toDouble())
		doubleArray.add(magneticFieldMean[2].toDouble())
		doubleArray.add(linAccelerationStd[0].toDouble())
		doubleArray.add(linAccelerationStd[1].toDouble())
		doubleArray.add(linAccelerationStd[2].toDouble())
		doubleArray.add(gyroscopeStd[0].toDouble())
		doubleArray.add(gyroscopeStd[1].toDouble())
		doubleArray.add(gyroscopeStd[2].toDouble())
		doubleArray.add(magneticFieldStd[0].toDouble())
		doubleArray.add(magneticFieldStd[1].toDouble())
		doubleArray.add(magneticFieldStd[2].toDouble())
		doubleArray.add(morning.toDouble())
		doubleArray.add(afternoon.toDouble())
		doubleArray.add(evening.toDouble())
		doubleArray.add(night.toDouble())
		doubleArray.add(weekday.toDouble())
		doubleArray.add(weekend.toDouble())
		doubleArray.add(wiredPhones.toDouble())
		doubleArray.add(orientation.toDouble())
		doubleArray.add(batteryPlugged.toDouble())
		doubleArray.add(audioVolume.toDouble())
		doubleArray.add(audioBtScoOn.toDouble())
		doubleArray.add(audioMusicActive.toDouble())
		doubleArray.add(audioSpeakerOn.toDouble())
		doubleArray.add(audioHeadsetOn.toDouble())
		doubleArray.add(ambientLight.toDouble())
		doubleArray.add(ambientLightMean.toDouble())
		doubleArray.add(ambientLightStd.toDouble())
		doubleArray.add(wifiConnected.toDouble())
		doubleArray.addAll(customFeaturesArray.map { it.toDouble() })
		return doubleArray.toTypedArray()
	}

	internal fun historyToArray(): Array<DoubleArray> {
		return modelDataHistory
			.takeLast(historySize)
			.map { line -> line.split(",")
				.filter { !it.contains("_") }
				.map { it.toDouble() }.toDoubleArray() }
			.toTypedArray()
	}
}
