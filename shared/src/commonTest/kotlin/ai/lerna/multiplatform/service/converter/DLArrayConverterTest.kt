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
}

