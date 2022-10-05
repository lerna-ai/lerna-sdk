package ai.lerna.multiplatform.service.converter

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DLArrayConverterTest {
	private val dlArrayConverter: DLArrayConverter = DLArrayConverter()

	@Test
	fun convert_doubleArray() {
		// Given
		val array: DoubleArray = doubleArrayOf(0.1000, 0.2000, 0.3000, 0.4000, 0.5000)
		// When
		val d2Array = dlArrayConverter.convert(array)
		// Then
		assertEquals(d2Array.dim, D2)
		assertEquals(d2Array.shape[0], 5)
		assertEquals(d2Array.shape[1], 1)
		assertEquals(d2Array.data.size, 5)
		assertEquals(d2Array.data[0], 0.1)
		assertEquals(d2Array.data[1], 0.2)
		assertEquals(d2Array.data[2], 0.3)
		assertEquals(d2Array.data[3], 0.4)
		assertEquals(d2Array.data[4], 0.5)
	}

	@Test
	fun convert_d2Array_to_doubleArray() {
		// Given
		val d2Array: D2Array<Double> = mk.ndarray(doubleArrayOf(0.1000, 0.2000, 0.3000, 0.4000, 0.5000)).reshape(5, 1)
		// When
		val array = dlArrayConverter.convert(d2Array)
		// Then
		assertEquals(array.size, 5)
		assertEquals(array[0], 0.1)
		assertEquals(array[1], 0.2)
		assertEquals(array[2], 0.3)
		assertEquals(array[3], 0.4)
		assertEquals(array[4], 0.5)
	}
}

