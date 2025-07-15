package ai.lerna.multiplatform.utils

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private const val historicDataSize = 60
private var fft = FFT()

internal fun ArrayDeque<Float>.std(): Float {
	if (this.size == 0) {
		return 0.0f
	}
	val mean = this.average().toFloat()
	return this.fold(0.0) { accumulator, next -> accumulator + (next - mean) * (next - mean) }
		.let { sqrt(it / this.size).toFloat() }
}

/**
 * Return Median and Median Absolute Deviation
 */
internal fun ArrayDeque<Float>.mad(): Pair<Float, Float> {
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
internal fun ArrayDeque<Float>.energy(): Float {
	return (this.fold(0.0) { accumulator, next -> accumulator + next * next } / this.size).toFloat()
}

/**
 * Compute Range of Data
 */
internal fun ArrayDeque<Float>.range(): Float {
	return (this.maxOrNull() ?: 0.0f) - (this.minOrNull() ?: 0.0f)
}

/**
 * Compute Signal magnitude area equivalent to sum of absolute value mean across three axis
 */
internal fun getSMA(x: ArrayDeque<Float>, y: ArrayDeque<Float>, z: ArrayDeque<Float>): Float {
	if (x.size == 0 || y.size == 0 || z.size == 0) {
		return 0.0f
	}
	val xMagnitude = x.fold(0.0) { accumulator, next -> accumulator + abs(next) } / x.size
	val yMagnitude = y.fold(0.0) { accumulator, next -> accumulator + abs(next) } / y.size
	val zMagnitude = z.fold(0.0) { accumulator, next -> accumulator + abs(next) } / z.size

	return (xMagnitude + yMagnitude + zMagnitude).toFloat()
}

/**
 * Compute Signal magnitude area equivalent to sum of absolute value mean across two axis
 */
internal fun getSMA(x: ArrayDeque<Float>, y: ArrayDeque<Float>): Float {
	if (x.size == 0 || y.size == 0) {
		return 0.0f
	}
	val xMagnitude = x.fold(0.0) { accumulator, next -> accumulator + abs(next) } / x.size
	val yMagnitude = y.fold(0.0) { accumulator, next -> accumulator + abs(next) } / y.size

	return (xMagnitude + yMagnitude).toFloat()
}

internal fun getAverageResultant(x: ArrayDeque<Float>, y: ArrayDeque<Float>, z: ArrayDeque<Float>): Float {
	if (x.size == 0 || y.size == 0 || z.size == 0) {
		return 0.0f
	}
	val n = listOf(x.size, y.size, z.size).min()
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

internal fun getAverageResultant(x: ArrayDeque<Float>, y: ArrayDeque<Float>): Float {
	if (x.size == 0 || y.size == 0) {
		return 0.0f
	}
	val n = listOf(x.size, y.size).min()
	var sum = 0.0

	for (i in 0 until n) {
		val xSquared = x[i].pow(2)
		val ySquared = y[i].pow(2)

		val magnitude = sqrt(xSquared + ySquared)
		sum += magnitude
	}

	return (sum / n).toFloat()
}

/**
 * Compute Inter quartile range (75th - 25th percentile value)
 */
internal fun ArrayDeque<Float>.iqr(): Float {
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
internal fun ArrayDeque<Float>.skewnessKurtosis(): Pair<Float, Float> {
	if (this.size < 4) {
		return Pair(0.0f, 0.0f)
	}
	val mean = this.average().toFloat()
	val std = this.std()
	val size = this.size
	if (std.equals(0.0f)) {
		return Pair(0.0f, 0.0f)
	}
	val skewness =
		this.map { ((it - mean) / std).pow(3) }.sum() * (size / ((size - 1) * (size - 2)))

	val kurtosis = this.map { ((it - mean) / std).pow(4) }
		.sum() * (size * (size + 1)) / ((size - 1) * (size - 2) * (size - 3))
	-3 * (size - 1).toFloat().pow(2) / ((size - 2) * (size - 3))

	return Pair(skewness, kurtosis)
}

internal fun ArrayDeque<Float>.numPeaks(): Int {
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

internal fun ArrayDeque<Float>.fft(): ArrayDeque<Float> {
	if (this.size == 0) {
		return this
	}
	return fft.fftReal(this)
}

internal fun getHistoricCalculations(deque: ArrayDeque<Float>): MutableList<Float> {
	val doubleArray: MutableList<Float> = ArrayList()

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
	return doubleArray
}

internal fun ArrayDeque<Float>.addBoxed(element: Float) {
	this.add(element)
	if (this.size > historicDataSize) {
		this.removeFirst()
	}
}

internal fun ArrayDeque<String>.addBoxed(element: String, historySize: Int) {
	this.add(element)
	if (this.size > historySize) {
		this.removeFirst()
	}
}