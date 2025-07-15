package ai.lerna.multiplatform.service.converter

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DLArrayConverterTest {
	private val dlArrayConverter: DLArrayConverter = DLArrayConverter()

	@Test
	fun convert_floatArray() {
		// Given
		val array: FloatArray = floatArrayOf(0.1000f, 0.2000f, 0.3000f, 0.4000f, 0.5000f)
		// When
		val d2Array = dlArrayConverter.convert(array)
		// Then
		assertEquals(d2Array.dim, D2)
		assertEquals(d2Array.shape[0], 5)
		assertEquals(d2Array.shape[1], 1)
		assertEquals(d2Array.data.size, 5)
		assertEquals(d2Array.data[0], 0.1f)
		assertEquals(d2Array.data[1], 0.2f)
		assertEquals(d2Array.data[2], 0.3f)
		assertEquals(d2Array.data[3], 0.4f)
		assertEquals(d2Array.data[4], 0.5f)
	}

	@Test
	fun convert_d2Array_to_floatArray() {
		// Given
		val d2Array: D2Array<Float> = mk.ndarray(floatArrayOf(0.1000f, 0.2000f, 0.3000f, 0.4000f, 0.5000f)).reshape(5, 1)
		// When
		val array = dlArrayConverter.convert(d2Array)
		// Then
		assertEquals(array.size, 5)
		assertEquals(array[0], 0.1f)
		assertEquals(array[1], 0.2f)
		assertEquals(array[2], 0.3f)
		assertEquals(array[3], 0.4f)
		assertEquals(array[4], 0.5f)
	}

	@Test
	fun convert2d_floatArray() {
		// Given
		val array: Array<FloatArray> = arrayOf(floatArrayOf(0.1000f, 0.2000f, 0.3000f, 0.4000f, 0.5000f),
			floatArrayOf(0.6000f, 0.7000f, 0.8000f, 0.9000f, 1.0000f))
		// When
		val d2Array = dlArrayConverter.convert2d(array)
		// Then
		assertEquals(d2Array.dim, D2)
		assertEquals(d2Array.shape[0], 2)
		assertEquals(d2Array.shape[1], 5)
		assertEquals(d2Array.data.size, 10)
		assertEquals(d2Array[0][0], 0.1f)
		assertEquals(d2Array[0][1], 0.2f)
		assertEquals(d2Array[0][2], 0.3f)
		assertEquals(d2Array[0][3], 0.4f)
		assertEquals(d2Array[0][4], 0.5f)
		assertEquals(d2Array[1][0], 0.6f)
		assertEquals(d2Array[1][1], 0.7f)
		assertEquals(d2Array[1][2], 0.8f)
		assertEquals(d2Array[1][3], 0.9f)
		assertEquals(d2Array[1][4], 1.0f)
	}

	@Test
	fun convert2d_d2Array_to_floatArray() {
		// Given
		val d2Array: D2Array<Float> = mk.ndarray(arrayOf(floatArrayOf(0.1000f, 0.2000f, 0.3000f, 0.4000f, 0.5000f),floatArrayOf(0.6000f, 0.7000f, 0.8000f, 0.9000f, 1.0000f)))
		// When
		val array = dlArrayConverter.convert2d(d2Array)
		// Then
		assertEquals(array.size, 2)
		assertEquals(array[0].size, 5)
		assertEquals(array[0][0], 0.1f)
		assertEquals(array[0][1], 0.2f)
		assertEquals(array[0][2], 0.3f)
		assertEquals(array[0][3], 0.4f)
		assertEquals(array[0][4], 0.5f)
		assertEquals(array[1][0], 0.6f)
		assertEquals(array[1][1], 0.7f)
		assertEquals(array[1][2], 0.8f)
		assertEquals(array[1][3], 0.9f)
		assertEquals(array[1][4], 1.0f)
	}
}

