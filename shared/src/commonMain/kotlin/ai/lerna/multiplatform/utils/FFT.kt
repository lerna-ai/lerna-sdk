package ai.lerna.multiplatform.utils

import kotlin.math.*


/**
 * Complex number representation
 */
data class Complex(val real: Float, val imag: Float) {
	operator fun plus(other: Complex) = Complex(real + other.real, imag + other.imag)
	operator fun minus(other: Complex) = Complex(real - other.real, imag - other.imag)
	operator fun times(other: Complex) = Complex(
		real * other.real - imag * other.imag,
		real * other.imag + imag * other.real
	)
}

class FFT {

	internal fun fftReal(input: ArrayDeque<Float>): ArrayDeque<Float> {
		val n = input.size
		if (n == 1) {
			return input
		}

		// Determine the next power of 2 greater than or equal to 2n-1
		val nextPowerOf2 = 2.0.pow(ceil(log2(2 * n - 1.toDouble()))).toInt()
		val paddedSize = nextPowerOf2 / 2

		// Pad the input with zeros to the next power of 2
		val paddedInput = FloatArray(paddedSize) { if (it < n) input[it] else 0.0F }

		// Convert real input to complex
		val complexInput = paddedInput.map { Complex(it, 0.0F) }.toTypedArray()

		// Perform FFT on complex input
		val complexResult = fft(complexInput)

		// Convert complex output to real
		return ArrayDeque(complexResult.map { sqrt(it.real.pow(2) + it.imag.pow(2)) }.toFloatArray().sliceArray(0 until n).toList())
	}


	private fun reverseBits(value: Int, numBits: Int): Int {
		var result = 0
		var num = value
		for (i in 0 until numBits) {
			result = result shl 1 or (num and 1)
			num = num shr 1
		}
		return result
	}

	/**
	 * Iterative FFT implementation (Cooley-Tukey radix-2 DIT)
	 */
	private fun fft(input: Array<Complex>): Array<Complex> {
		val n = input.size
		val m = log2(n.toFloat()).toInt()

		// Bit-reverse permutation
		val permutation = Array(n) { reverseBits(it, m) }
		val result = Array(n) { input[permutation[it]] }

		// Cooley-Tukey radix-2 DIT algorithm
		var stepSize = 2
		while (stepSize <= n) {
			val halfStep = stepSize / 2
			val angleIncrement = -2.0 * PI / stepSize

			for (offset in 0 until n step stepSize) {
				var angle = 0.0F

				for (k in 0 until halfStep) {
					val indexEven = offset + k
					val indexOdd = offset + k + halfStep

					val twiddle = Complex(
						cos(angle),
						sin(angle)
					)

					val even = result[indexEven]
					val odd = result[indexOdd] * twiddle

					result[indexEven] = even + odd
					result[indexOdd] = even - odd

					angle += angleIncrement.toFloat()
				}
			}

			stepSize *= 2
		}

		return result
	}
}

