package ai.lerna.multiplatform

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ModelDataTest {
	lateinit var modelData: ModelData

	@BeforeTest
	fun setUp() {
		modelData = ModelData()
	}

	@Test
	fun toCsv_zeros() {
		// When
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun proximity_toCsv() {
		// Given
		val proximity = 5F
		// When
		modelData.setProximity(proximity)
		val result = modelData.toCsv()
		// Then
		assertEquals("5.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun allData_toCsv() {
		// When
		modelData.setProximity(1F)
		modelData.setLinAcceleration(2F,3F,4F)
		modelData.setGyroscope(5F,6F,7F)
		modelData.setMagneticField(8F,9F,10F)
		modelData.setBrightness(11F)
		modelData.setDateTime(9, 3)
		modelData.setPhones(true)
		modelData.setOrientation(12F)
		modelData.setBatteryChargingState(2)
		modelData.setAudioActivity(2, 13F, 14F, 15F,16F, true, false, true, false, true)
		modelData.setLight(17F)
		modelData.setWifiConnected(true)
		val result = modelData.toCsv()
		// Then
		assertEquals("1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,11.0,1.0,0.0,0.0,0.0,1.0,0.0,1.0,12.0,0.0,1.0,0.0,0.0,0.0,1.0,13.0,14.0,15.0,16.0,1.0,0.0,1.0,0.0,1.0,17.0,0.0,0.0,1.0", result)
	}

	@Test
	fun toCsv_ambientLight() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setLight(1F)
		modelData.setHistory()
		modelData.setLight(2F)
		modelData.setHistory()
		modelData.setLight(3F)
		modelData.setHistory()
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,3.0,2.0,0.8164966106414795,0.0", result)
	}

	@Test
	fun toCsv_ambientLight_single() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setLight(1F)
		modelData.setHistory()
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,1.0,0.0,0.0", result)
	}

	@Test
	fun toCsv_linAcceleration() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setLinAcceleration(1F, 4F, 7F)
		modelData.setHistory()
		modelData.setLinAcceleration(2F, 5F, 8F)
		modelData.setHistory()
		modelData.setLinAcceleration(3F, 6F, 9F)
		modelData.setHistory()
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,3.0,6.0,9.0,0.0,0.0,0.0,0.0,0.0,0.0,2.0,5.0,8.0,0.0,0.0,0.0,0.0,0.0,0.0,0.8164966106414795,0.8164966106414795,0.8164966106414795,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun toCsv_linAccelerationt_single() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setLinAcceleration(1F, 4F, 7F)
		modelData.setHistory()
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,1.0,4.0,7.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,4.0,7.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun toCsv_gyroscope() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setGyroscope(1F, 4F, 7F)
		modelData.setHistory()
		modelData.setGyroscope(2F, 5F, 8F)
		modelData.setHistory()
		modelData.setGyroscope(3F, 6F, 9F)
		modelData.setHistory()
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,3.0,6.0,9.0,0.0,0.0,0.0,0.0,0.0,0.0,2.0,5.0,8.0,0.0,0.0,0.0,0.0,0.0,0.0,0.8164966106414795,0.8164966106414795,0.8164966106414795,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun toCsv_gyroscope_single() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setGyroscope(1F, 4F, 7F)
		modelData.setHistory()
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,1.0,4.0,7.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,4.0,7.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun toCsv_magneticField() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setMagneticField(1F, 4F, 7F)
		modelData.setHistory()
		modelData.setMagneticField(2F, 5F, 8F)
		modelData.setHistory()
		modelData.setMagneticField(3F, 6F, 9F)
		modelData.setHistory()
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,0.0,0.0,0.0,3.0,6.0,9.0,0.0,0.0,0.0,0.0,0.0,0.0,2.0,5.0,8.0,0.0,0.0,0.0,0.0,0.0,0.0,0.8164966106414795,0.8164966106414795,0.8164966106414795,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun toCsv_magneticField_single() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setMagneticField(1F, 4F, 7F)
		modelData.setHistory()
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,4.0,7.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,4.0,7.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun setupCustomFeatureSize() {
		// Given
		modelData.resetSensorHistory()
		// When
		modelData.setupCustomFeatureSize(5)
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0", result)
	}

	@Test
	fun updateCustomFeatures() {
		// Given
		modelData.resetSensorHistory()
		modelData.setupCustomFeatureSize(5)
		// When
		modelData.updateCustomFeatures(arrayOf(1F, 2F, 3F, 4F, 5F).toFloatArray())
		val result = modelData.toCsv()
		// Then
		assertEquals("0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,1.0,2.0,3.0,4.0,5.0", result)
	}
}