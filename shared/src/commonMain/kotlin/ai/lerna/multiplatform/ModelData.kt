package ai.lerna.multiplatform

import kotlin.math.sqrt

internal class ModelData {
	private var proximity: Float = 0F
	private var linAcceleration = arrayOf(0F, 0F, 0F)
	private var gyroscope = arrayOf(0F, 0F, 0F)
	private var magneticField = arrayOf(0F, 0F, 0F)
	private var linAccelerationMean = arrayOf(0F, 0F, 0F)
	private var gyroscopeMean = arrayOf(0F, 0F, 0F)
	private var magneticFieldMean = arrayOf(0F, 0F, 0F)
	private var linAccelerationStd = arrayOf(0F, 0F, 0F)
	private var gyroscopeStd = arrayOf(0F, 0F, 0F)
	private var magneticFieldStd = arrayOf(0F, 0F, 0F)
	private var brightness: Float = 0F
	private var morning: Float = 0F
	private var afternoon: Float = 0F
	private var evening: Float = 0F
	private var night: Float = 0F
	private var weekday: Float = 0F
	private var weekend: Float = 0F
	private var wiredPhones: Float = 0F
	private var linAccelerationHistoryX: MutableList<Float> = ArrayList()
	private var linAccelerationHistoryY: MutableList<Float> = ArrayList()
	private var linAccelerationHistoryZ: MutableList<Float> = ArrayList()
	private var gyroscopeHistoryX: MutableList<Float> = ArrayList()
	private var gyroscopeHistoryY: MutableList<Float> = ArrayList()
	private var gyroscopeHistoryZ: MutableList<Float> = ArrayList()
	private var magneticFieldHistoryX: MutableList<Float> = ArrayList()
	private var magneticFieldHistoryY: MutableList<Float> = ArrayList()
	private var magneticFieldHistoryZ: MutableList<Float> = ArrayList()
	private var orientation: Float = 0F
	private var batteryPluggedAC: Float = 0F
	private var batteryPluggedUSB: Float = 0F
	private var batteryPluggedWireless: Float = 0F
	private var audioRingerModeSilent: Float = 0F
	private var audioRingerModeVibrate: Float = 0F
	private var audioRingerModeNormal: Float = 0F
	private var audioAlarmVolume: Float = 0F
	private var audioMusicVolume: Float = 0F
	private var audioNotificationVolume: Float = 0F
	private var audioRingVolume: Float = 0F
	private var audioBtScoOn: Float = 0F
	private var audioMicMute: Float = 0F
	private var audioMusicActive: Float = 0F
	private var audioSpeakerOn: Float = 0F
	private var audioHeadsetOn: Float = 0F
	private var ambientLight: Float = 0F
	private var ambientLightHistory: MutableList<Float> = ArrayList()
	private var ambientLightMean: Float = 0F
	private var ambientLightStd: Float = 0F
	private var wifiConnected: Float = 0F
	private var customFeaturesArray: FloatArray = FloatArray(0)

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

	internal fun setProximity(proximity: Float) {
		this.proximity = proximity
	}

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

	internal fun setBrightness(_brightness: Float) {
		brightness = _brightness
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
		when (chargingState) {
			1 -> { // BATTERY_PLUGGED_AC
				batteryPluggedAC = 1F
				batteryPluggedUSB = 0F
				batteryPluggedWireless = 0F
			}
			2 -> { // BATTERY_PLUGGED_USB
				batteryPluggedAC = 0F
				batteryPluggedUSB = 1F
				batteryPluggedWireless = 0F
			}
			4 -> { // BATTERY_PLUGGED_WIRELESS
				batteryPluggedAC = 0F
				batteryPluggedUSB = 0F
				batteryPluggedWireless = 1F
			}
			else -> {
				batteryPluggedAC = 0F
				batteryPluggedUSB = 0F
				batteryPluggedWireless = 0F
			}
		}
	}

	internal fun setAudioActivity(
		ringerMode: Int,
		alarmVolume: Float,
		musicVolume: Float,
		notificationVolume: Float,
		ringVolume: Float,
		isBtScoOn: Boolean,
		isMicMute: Boolean,
		isMusicActive: Boolean,
		isSpeakerOn: Boolean,
		isHeadsetOn: Boolean
	) {
		when (ringerMode) {
			0 -> { // RINGER_MODE_SILENT
				audioRingerModeSilent = 1F
				audioRingerModeVibrate = 0F
				audioRingerModeNormal = 0F
			}
			1 -> { // RINGER_MODE_VIBRATE
				audioRingerModeSilent = 0F
				audioRingerModeVibrate = 1F
				audioRingerModeNormal = 0F
			}
			2 -> { // RINGER_MODE_NORMAL
				audioRingerModeSilent = 0F
				audioRingerModeVibrate = 0F
				audioRingerModeNormal = 1F
			}
		}

		audioAlarmVolume = alarmVolume
		audioMusicVolume = musicVolume
		audioNotificationVolume = notificationVolume
		audioRingVolume = ringVolume
		audioBtScoOn = if (isBtScoOn) 1F else 0F
		audioMicMute = if (isMicMute) 1F else 0F
		audioMusicActive = if (isMusicActive) 1F else 0F
		audioSpeakerOn = if (isSpeakerOn) 1F else 0F
		audioHeadsetOn = if (isHeadsetOn) 1F else 0F
	}

	internal fun setWifiConnected(isConnectionUnmetered: Boolean) {
		wifiConnected = if (isConnectionUnmetered) 1F else 0F
	}

//	internal fun updateData(context: Context) {
//
//		//setPhones(isWiredHeadsetOn(context))
//
//		//setOrientation(context)
//
//		//setBatteryChargingState(context)
//
//		//setAudioActivity(context)
//
//		//setWifiConnected(context)
//
//		// setBluetooth(context) -- removed
//
//		//setHistory()
//	}

	internal fun resetSensorHistory() {
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

		if (dayOfWeek == 7 || // Calendar.SATURDAY
			dayOfWeek == 1    // Calendar.SUNDAY
		) {
			weekend = 1F
			weekday = 0F
		} else {
			weekend = 0F
			weekday = 1F
		}
	}

	private fun MutableList<Float>.std(): Float {
		val mean = this.average().toFloat()
		return this.fold(0.0) { accumulator, next -> accumulator + (next - mean) * (next - mean) }
			.let { sqrt(it / this.size).toFloat() }
	}

	internal fun setHistory() {
		linAccelerationHistoryX.add(linAcceleration[0])
		linAccelerationHistoryY.add(linAcceleration[1])
		linAccelerationHistoryZ.add(linAcceleration[2])
		linAccelerationHistoryX = linAccelerationHistoryX.takeLast(50).toMutableList()
		linAccelerationHistoryY = linAccelerationHistoryY.takeLast(50).toMutableList()
		linAccelerationHistoryZ = linAccelerationHistoryZ.takeLast(50).toMutableList()
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

		gyroscopeHistoryX.add(gyroscope[0])
		gyroscopeHistoryY.add(gyroscope[1])
		gyroscopeHistoryZ.add(gyroscope[2])
		gyroscopeHistoryX = gyroscopeHistoryX.takeLast(50).toMutableList()
		gyroscopeHistoryY = gyroscopeHistoryY.takeLast(50).toMutableList()
		gyroscopeHistoryZ = gyroscopeHistoryZ.takeLast(50).toMutableList()
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

		magneticFieldHistoryX.add(magneticField[0])
		magneticFieldHistoryY.add(magneticField[1])
		magneticFieldHistoryZ.add(magneticField[2])
		magneticFieldHistoryX = magneticFieldHistoryX.takeLast(50).toMutableList()
		magneticFieldHistoryY = magneticFieldHistoryY.takeLast(50).toMutableList()
		magneticFieldHistoryZ = magneticFieldHistoryZ.takeLast(50).toMutableList()
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

		ambientLightHistory.add(ambientLight)
		ambientLightHistory = ambientLightHistory.takeLast(50).toMutableList()
		if (ambientLightHistory.isNotEmpty()) {
			ambientLightMean = ambientLightHistory.average().toFloat()
			ambientLightStd = ambientLightHistory.std()
		}
	}

	internal fun toCsv(): String {
		return toArray().joinToString(separator = ",") { it.toString() }
	}

	private fun toArray(): Array<Double> {
		val doubleArray: MutableList<Double> = ArrayList()
		doubleArray.add(proximity.toDouble())
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
		doubleArray.add(brightness.toDouble())
		doubleArray.add(morning.toDouble())
		doubleArray.add(afternoon.toDouble())
		doubleArray.add(evening.toDouble())
		doubleArray.add(night.toDouble())
		doubleArray.add(weekday.toDouble())
		doubleArray.add(weekend.toDouble())
		doubleArray.add(wiredPhones.toDouble())
		doubleArray.add(orientation.toDouble())
		doubleArray.add(batteryPluggedAC.toDouble())
		doubleArray.add(batteryPluggedUSB.toDouble())
		doubleArray.add(batteryPluggedWireless.toDouble())
		doubleArray.add(audioRingerModeSilent.toDouble())
		doubleArray.add(audioRingerModeVibrate.toDouble())
		doubleArray.add(audioRingerModeNormal.toDouble())
		doubleArray.add(audioAlarmVolume.toDouble())
		doubleArray.add(audioMusicVolume.toDouble())
		doubleArray.add(audioNotificationVolume.toDouble())
		doubleArray.add(audioRingVolume.toDouble())
		doubleArray.add(audioBtScoOn.toDouble())
		doubleArray.add(audioMicMute.toDouble())
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
}
