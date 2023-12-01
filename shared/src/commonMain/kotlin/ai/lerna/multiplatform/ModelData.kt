package ai.lerna.multiplatform

import ai.lerna.multiplatform.utils.DateUtil
import ai.lerna.multiplatform.utils.FFT
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class ModelData(_historySize: Int = 1) {
	private val historicDataSize = 60
	private val historySize = _historySize

	//private var proximity: Float = 0F
	private var linAcceleration = arrayOf(0F, 0F, 0F)
	private var gyroscope = arrayOf(0F, 0F, 0F)
	private var magneticField = arrayOf(0F, 0F, 0F)
	private var magneticFieldMean = arrayOf(0F, 0F, 0F)
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

	private var fft = FFT()
	private var linAccelerationHistoryXFft: ArrayDeque<Float> = ArrayDeque()
	private var linAccelerationHistoryYFft: ArrayDeque<Float> = ArrayDeque()
	private var linAccelerationHistoryZFft: ArrayDeque<Float> = ArrayDeque()

	private var gyroscopeHistoryXFft: ArrayDeque<Float> = ArrayDeque()
	private var gyroscopeHistoryYFft: ArrayDeque<Float> = ArrayDeque()
	private var gyroscopeHistoryZFft: ArrayDeque<Float> = ArrayDeque()

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

	/**
	 * Return Median and Median Absolute Deviation
	 */
	private fun ArrayDeque<Float>.mad(): Pair<Float, Float> {
		val sortedDeque = this.sorted()
		val size = this.size
		if (size < 3) {
			return Pair(0.0f, 0.0f)
		}

		val median = if (size % 2 == 1) {
			sortedDeque[size / 2]
		} else {
			val midIndex = size / 2
			(sortedDeque[midIndex - 1] + sortedDeque[midIndex]) / 2.0
		}
		val absoluteDeviations = sortedDeque.map { abs(it - median.toFloat()) }

		val mad = if (size % 2 == 1) {
			absoluteDeviations[size / 2]
		} else {
			val midIndex = size / 2
			((absoluteDeviations[midIndex - 1] + absoluteDeviations[midIndex]) / 2.0).toFloat()
		}

		return Pair(median.toFloat(), mad)
	}

	/**
	 * Compute Energy of signal equivalent to mean of sum of squares of the values
	 */
	private fun ArrayDeque<Float>.energy(): Float {
		return (this.fold(0.0) { accumulator, next -> accumulator + next * next } / this.size).toFloat()
	}

	/**
	 * Compute Range of Data
	 */
	private fun ArrayDeque<Float>.range(): Float {
		return (this.maxOrNull() ?: 0.0f) - (this.minOrNull() ?: 0.0f)
	}

	private fun getAverageResultant(x: ArrayDeque<Float>, y: ArrayDeque<Float>, z: ArrayDeque<Float>): Float {
		if (x.size == 0 || y.size == 0 || z.size == 0) {
			return 0.0f
		}
		val n = x.size
		var sum = 0.0

		for (i in 0 until n) {
			val xSquared = x[i].pow(2)
			val ySquared = y[i].pow(2)
			val zSquared = z[i].pow(2)

			val magnitude = sqrt(xSquared + ySquared + zSquared)
			sum += magnitude
		}

		return (sum / n).toFloat()
	}

	/**
	 * Compute Signal magnitude area equivalent to sum of absolute value mean across three axis
	 */
	private fun getSMA(x: ArrayDeque<Float>, y: ArrayDeque<Float>, z: ArrayDeque<Float>): Float {
		if (x.size == 0 || y.size == 0 || z.size == 0) {
			return 0.0f
		}
		val xMagnitude = x.fold(0.0) { accumulator, next -> accumulator + abs(next) } / x.size
		val yMagnitude = y.fold(0.0) { accumulator, next -> accumulator + abs(next) } / y.size
		val zMagnitude = z.fold(0.0) { accumulator, next -> accumulator + abs(next) } / z.size

		return (xMagnitude + yMagnitude + zMagnitude).toFloat()
	}

	/**
	 * Compute Inter quartile range (75th - 25th percentile value)
	 */
	private fun ArrayDeque<Float>.iqr(): Float {
		if (this.size == 0) {
			return 0.0f
		}
		val sortedDeque = this.sorted()
		val upperIndex = (0.75 * this.size).toInt()
		val lowerIndex = (0.25 * this.size).toInt()
		return sortedDeque.get(upperIndex) - sortedDeque.get(lowerIndex)
	}

	/**
	 * Compute skewness and kurotis of the a distribution using Fisher-Pearson standardized moment coefficient
	 * and unbiased estimator formulae
	 */
	private fun ArrayDeque<Float>.skewnessKurtosis(): Pair<Float, Float> {
		val mean = this.average().toFloat()
		val std = this.std()
		val size = this.size
		if (std.equals(0.0f) || size < 4) {
			return Pair(0.0f, 0.0f)
		}
		val skewness =
			this.map { ((it - mean) / std).pow(3) }.sum() * (size / ((size - 1) * (size - 2)))

		val kurtosis = this.map { ((it - mean) / std).pow(4) }
			.sum() * (size * (size + 1)) / ((size - 1) * (size - 2) * (size - 3))
		-3 * (size - 1).toFloat().pow(2) / ((size - 2) * (size - 3))

		return Pair(skewness, kurtosis)
	}

	private fun ArrayDeque<Float>.fft(): ArrayDeque<Float> {
		return fft.fftReal(this)
	}

	private fun ArrayDeque<String>.addBoxed(element: String) {
		this.add(element)
		if (this.size > historySize) {
			this.removeFirst()
		}
	}

	private fun ArrayDeque<Float>.numPeaks(): Int {
		var peaksCount = 0
		val size = this.size

		if (size < 3) {
			return peaksCount
		}

		for (i in 1 until size - 1) {
			if (this[i] > this[i - 1] && this[i] > this[i + 1]) {
				peaksCount++
			}
		}
		return peaksCount
	}

	internal fun setHistory(time: String = DateUtil().now()) {
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
				postfix = ",$successValue\n"
			) { it }
	}

	internal fun toCsv(): String {
		return toArray().joinToString(separator = ",") { it.toString() }
	}

	internal fun toArray(): Array<Float> {
		val doubleArray: MutableList<Float> = ArrayList()
		//doubleArray.add(proximity)

		val historyDeques = listOf(
			linAccelerationHistoryX, linAccelerationHistoryY, linAccelerationHistoryZ,
			gyroscopeHistoryX, gyroscopeHistoryY, gyroscopeHistoryZ,
			linAccelerationHistoryXFft, linAccelerationHistoryYFft, linAccelerationHistoryZFft,
			gyroscopeHistoryXFft, gyroscopeHistoryYFft, gyroscopeHistoryZFft
		)

		// NEW: Total Num of features for Accelerometer + Gyroscope now = 12*12 + 6 = 150
		for (deque in historyDeques) {

			// Return Median and Median Absolute Deviation as Pair<Float, Float> for efficiency
			val (median, mad) = deque.mad()
			val (skewness, kurtosis) = deque.skewnessKurtosis()

			doubleArray.add(if (deque.size == 0) 0.0f else deque.average().toFloat())
			doubleArray.add(if (deque.size == 0) 0.0f else deque.std())
			doubleArray.add(deque.minOrNull() ?: 0.0f)
			doubleArray.add(deque.maxOrNull() ?: 0.0f)
			doubleArray.add(deque.range())
			doubleArray.add(median)
			doubleArray.add(mad)
			doubleArray.add(deque.numPeaks().toFloat())
			doubleArray.add(deque.iqr())
			doubleArray.add(if (deque.size == 0) 0.0f else deque.energy())
			doubleArray.add(skewness)
			doubleArray.add(kurtosis)
		}

		doubleArray.add(getSMA(linAccelerationHistoryX, linAccelerationHistoryY, linAccelerationHistoryZ))
		doubleArray.add(getAverageResultant(linAccelerationHistoryX, linAccelerationHistoryY, linAccelerationHistoryZ))
		doubleArray.add(getSMA(linAccelerationHistoryXFft, linAccelerationHistoryYFft, linAccelerationHistoryZFft))

		doubleArray.add(getSMA(gyroscopeHistoryX, gyroscopeHistoryY, gyroscopeHistoryZ))
		doubleArray.add(getAverageResultant(gyroscopeHistoryX, gyroscopeHistoryY, gyroscopeHistoryZ))
		doubleArray.add(getSMA(gyroscopeHistoryXFft, gyroscopeHistoryYFft, gyroscopeHistoryZFft))


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

	internal fun historyToArray(): Array<FloatArray> {
		return modelDataHistory
			.takeLast(historySize)
			.map { line ->
				line.split(",")
					.filter { !it.contains("_") }
					.map { it.toFloat() }.toFloatArray()
			}
			.toTypedArray()
	}

	internal fun isHistoryNonEmpty(): Boolean {
		return modelDataHistory.isNotEmpty()
	}
}
