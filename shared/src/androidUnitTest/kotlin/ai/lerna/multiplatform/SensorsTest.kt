package ai.lerna.multiplatform

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class SensorsTest {
	private val modelData: ModelData = ModelData(50)
	private val sensors: Sensors = Sensors(ApplicationProvider.getApplicationContext(), modelData)

	@Test
	fun calcBrightness(){
		// Given
		val light = 100f
		val maxValue = 100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(1f, result)
	}

	@Test
	fun calcBrightness_0f(){
		// Given
		val light = 0f
		val maxValue = 100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.0f, result)
	}

	@Test
	fun calcBrightness_0_5f(){
		// Given
		val light = 0.5f
		val maxValue = 100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.0878558f, result)
	}

	@Test
	fun calcBrightness_minus_0_5f(){
		// Given
		val light = -0.5f
		val maxValue = 100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.0f, result)
	}

	@Test
	fun calcBrightness_10f(){
		// Given
		val light = 10f
		val maxValue = 100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.5195737f, result)
	}

	@Test
	fun calcBrightness_12_35f(){
		// Given
		val light = 12.35f
		val maxValue = 100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.5615274f, result)
	}

	@Test
	fun calcBrightness_negative_light(){
		// Given
		val light = -12.35f
		val maxValue = 100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.0f, result)
	}

	@Test
	fun calcBrightness_negative_maxValue(){
		// Given
		val light = 12.35f
		val maxValue = -100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.5f, result)
	}

	@Test
	fun calcBrightness_negative_both(){
		// Given
		val light = -12.35f
		val maxValue = -100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.5f, result)
	}

	@Test
	fun calcBrightness_NaN_value(){
		// Given
		val light = Float.NaN
		val maxValue = 100f
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.5f, result)
	}

	@Test
	fun calcBrightness_NaN_maxValue(){
		// Given
		val light = 100f
		val maxValue = Float.NaN
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.5f, result)
	}

	@Test
	fun calcBrightness_NaN_all(){
		// Given
		val light = Float.NaN
		val maxValue = Float.NaN
		// When
		val result = sensors.calcBrightness(light, maxValue)
		// Then
		assertEquals(0.5f, result)
	}
}