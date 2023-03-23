package ai.lerna.multiplatform

import com.soywiz.kds.toFloatList
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class MergeInputDataTest {
	lateinit var modelData: ModelData
	lateinit var inferenceData: MergeInputData

	@BeforeTest
	fun setUp() {
		modelData = ModelData(50)
		inferenceData = MergeInputData(modelData, 5)
	}

	@Test
	fun getInferenceInputData() {
		// Given
		modelData.setGyroscope(5F, 6F, 7F)
		modelData.setMagneticField(8F, 9F, 10F)
		modelData.setDateTime(9, 3)
		modelData.setPhones(true)
		modelData.setOrientation(12F)
		modelData.setBatteryChargingState(2)
		modelData.setAudioActivity(
			volume = 13F,
			isBtScoOn = true,
			isMusicActive = false,
			isSpeakerOn = true,
			isHeadsetOn = false
		)
		modelData.setLight(17F)
		modelData.setWifiConnected(true)
		for (i in 1..10) {
			modelData.setLinAcceleration(i.toFloat(), i.toFloat(), i.toFloat())
			modelData.setHistory()
		}
		inferenceData.putItem("first", floatArrayOf(1f, 2f, 3f, 4f, 5f))
		inferenceData.putItem("second", floatArrayOf(6f, 7f, 8f, 9f, 10f))
		// When
		val result = inferenceData.getMergedInputData()
		// Then
		assertEquals(2, result.first.size)
		assertEquals(100, result.second.size)
		assertContentEquals(
			floatArrayOf(
				10f, 10f, 10f, 5f, 6f, 7f, 8f, 9f, 10f, 5.5f, 5.5f, 5.5f, 5f, 6f, 7f, 8f, 9f, 10f, 2.8722813f, 2.8722813f, 2.8722813f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				10f, 10f, 10f, 5f, 6f, 7f, 8f, 9f, 10f, 5.5f, 5.5f, 5.5f, 5f, 6f, 7f, 8f, 9f, 10f, 2.8722813f, 2.8722813f, 2.8722813f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f
			),
			result.second.data.toFloatList().toFloatArray()
		)
	}


	@Test
	fun getInferenceInputDataHistory() {
		// Given
		modelData.setGyroscope(5F, 6F, 7F)
		modelData.setMagneticField(8F, 9F, 10F)
		modelData.setDateTime(9, 3)
		modelData.setPhones(true)
		modelData.setOrientation(12F)
		modelData.setBatteryChargingState(2)
		modelData.setAudioActivity(
			volume = 13F,
			isBtScoOn = true,
			isMusicActive = false,
			isSpeakerOn = true,
			isHeadsetOn = false
		)
		modelData.setLight(17F)
		modelData.setWifiConnected(true)
		for (i in 1..55) {
			modelData.setLinAcceleration(i.toFloat(), i.toFloat(), i.toFloat())
			modelData.setHistory()
		}
		inferenceData.putItem("first", floatArrayOf(1f, 2f, 3f, 4f, 5f))
		inferenceData.putItem("second", floatArrayOf(6f, 7f, 8f, 9f, 10f))
		// When
		val result = inferenceData.getMergedInputDataHistory()
		// Then
		assertEquals(2, result.size)
		assertContentEquals(arrayOf("first", "second"), result.keys.toTypedArray())
		assertEquals(2500, result["first"]?.size)
		assertContentEquals(
			floatArrayOf(
				6f, 6f, 6f, 5f, 6f, 7f, 8f, 9f, 10f, 3.5f, 3.5f, 3.5f, 5f, 6f, 7f, 8f, 9f, 10f, 1.7078252f, 1.7078252f, 1.7078252f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				7f, 7f, 7f, 5f, 6f, 7f, 8f, 9f, 10f, 4f, 4f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 2f, 2f, 2f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				8f, 8f, 8f, 5f, 6f, 7f, 8f, 9f, 10f, 4.5f, 4.5f, 4.5f, 5f, 6f, 7f, 8f, 9f, 10f, 2.291288f, 2.291288f, 2.291288f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				9f, 9f, 9f, 5f, 6f, 7f, 8f, 9f, 10f, 5f, 5f, 5f, 5f, 6f, 7f, 8f, 9f, 10f, 2.5819888f, 2.5819888f, 2.5819888f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				10f, 10f, 10f, 5f, 6f, 7f, 8f, 9f, 10f, 5.5f, 5.5f, 5.5f, 5f, 6f, 7f, 8f, 9f, 10f, 2.8722813f, 2.8722813f, 2.8722813f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				11f, 11f, 11f, 5f, 6f, 7f, 8f, 9f, 10f, 6f, 6f, 6f, 5f, 6f, 7f, 8f, 9f, 10f, 3.1622777f, 3.1622777f, 3.1622777f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				12f, 12f, 12f, 5f, 6f, 7f, 8f, 9f, 10f, 6.5f, 6.5f, 6.5f, 5f, 6f, 7f, 8f, 9f, 10f, 3.4520526f, 3.4520526f, 3.4520526f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				13f, 13f, 13f, 5f, 6f, 7f, 8f, 9f, 10f, 7f, 7f, 7f, 5f, 6f, 7f, 8f, 9f, 10f, 3.7416575f, 3.7416575f, 3.7416575f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				14f, 14f, 14f, 5f, 6f, 7f, 8f, 9f, 10f, 7.5f, 7.5f, 7.5f, 5f, 6f, 7f, 8f, 9f, 10f, 4.031129f, 4.031129f, 4.031129f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				15f, 15f, 15f, 5f, 6f, 7f, 8f, 9f, 10f, 8f, 8f, 8f, 5f, 6f, 7f, 8f, 9f, 10f, 4.3204937f, 4.3204937f, 4.3204937f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				16f, 16f, 16f, 5f, 6f, 7f, 8f, 9f, 10f, 8.5f, 8.5f, 8.5f, 5f, 6f, 7f, 8f, 9f, 10f, 4.609772f, 4.609772f, 4.609772f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				17f, 17f, 17f, 5f, 6f, 7f, 8f, 9f, 10f, 9f, 9f, 9f, 5f, 6f, 7f, 8f, 9f, 10f, 4.8989797f, 4.8989797f, 4.8989797f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				18f, 18f, 18f, 5f, 6f, 7f, 8f, 9f, 10f, 9.5f, 9.5f, 9.5f, 5f, 6f, 7f, 8f, 9f, 10f, 5.1881275f, 5.1881275f, 5.1881275f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				19f, 19f, 19f, 5f, 6f, 7f, 8f, 9f, 10f, 10f, 10f, 10f, 5f, 6f, 7f, 8f, 9f, 10f, 5.477226f, 5.477226f, 5.477226f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				20f, 20f, 20f, 5f, 6f, 7f, 8f, 9f, 10f, 10.5f, 10.5f, 10.5f, 5f, 6f, 7f, 8f, 9f, 10f, 5.766281f, 5.766281f, 5.766281f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				21f, 21f, 21f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 11f, 11f, 5f, 6f, 7f, 8f, 9f, 10f, 6.0553007f, 6.0553007f, 6.0553007f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				22f, 22f, 22f, 5f, 6f, 7f, 8f, 9f, 10f, 11.5f, 11.5f, 11.5f, 5f, 6f, 7f, 8f, 9f, 10f, 6.344289f, 6.344289f, 6.344289f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				23f, 23f, 23f, 5f, 6f, 7f, 8f, 9f, 10f, 12f, 12f, 12f, 5f, 6f, 7f, 8f, 9f, 10f, 6.6332498f, 6.6332498f, 6.6332498f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				24f, 24f, 24f, 5f, 6f, 7f, 8f, 9f, 10f, 12.5f, 12.5f, 12.5f, 5f, 6f, 7f, 8f, 9f, 10f, 6.9221864f, 6.9221864f, 6.9221864f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				25f, 25f, 25f, 5f, 6f, 7f, 8f, 9f, 10f, 13f, 13f, 13f, 5f, 6f, 7f, 8f, 9f, 10f, 7.2111025f, 7.2111025f, 7.2111025f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				26f, 26f, 26f, 5f, 6f, 7f, 8f, 9f, 10f, 13.5f, 13.5f, 13.5f, 5f, 6f, 7f, 8f, 9f, 10f, 7.5f, 7.5f, 7.5f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				27f, 27f, 27f, 5f, 6f, 7f, 8f, 9f, 10f, 14f, 14f, 14f, 5f, 6f, 7f, 8f, 9f, 10f, 7.788881f, 7.788881f, 7.788881f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				28f, 28f, 28f, 5f, 6f, 7f, 8f, 9f, 10f, 14.5f, 14.5f, 14.5f, 5f, 6f, 7f, 8f, 9f, 10f, 8.077747f, 8.077747f, 8.077747f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				29f, 29f, 29f, 5f, 6f, 7f, 8f, 9f, 10f, 15f, 15f, 15f, 5f, 6f, 7f, 8f, 9f, 10f, 8.3666f, 8.3666f, 8.3666f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				30f, 30f, 30f, 5f, 6f, 7f, 8f, 9f, 10f, 15.5f, 15.5f, 15.5f, 5f, 6f, 7f, 8f, 9f, 10f, 8.655441f, 8.655441f, 8.655441f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				31f, 31f, 31f, 5f, 6f, 7f, 8f, 9f, 10f, 16f, 16f, 16f, 5f, 6f, 7f, 8f, 9f, 10f, 8.944272f, 8.944272f, 8.944272f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				32f, 32f, 32f, 5f, 6f, 7f, 8f, 9f, 10f, 16.5f, 16.5f, 16.5f, 5f, 6f, 7f, 8f, 9f, 10f, 9.233092f, 9.233092f, 9.233092f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				33f, 33f, 33f, 5f, 6f, 7f, 8f, 9f, 10f, 17f, 17f, 17f, 5f, 6f, 7f, 8f, 9f, 10f, 9.521905f, 9.521905f, 9.521905f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				34f, 34f, 34f, 5f, 6f, 7f, 8f, 9f, 10f, 17.5f, 17.5f, 17.5f, 5f, 6f, 7f, 8f, 9f, 10f, 9.810708f, 9.810708f, 9.810708f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				35f, 35f, 35f, 5f, 6f, 7f, 8f, 9f, 10f, 18f, 18f, 18f, 5f, 6f, 7f, 8f, 9f, 10f, 10.099504f, 10.099504f, 10.099504f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				36f, 36f, 36f, 5f, 6f, 7f, 8f, 9f, 10f, 18.5f, 18.5f, 18.5f, 5f, 6f, 7f, 8f, 9f, 10f, 10.388294f, 10.388294f, 10.388294f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				37f, 37f, 37f, 5f, 6f, 7f, 8f, 9f, 10f, 19f, 19f, 19f, 5f, 6f, 7f, 8f, 9f, 10f, 10.677078f, 10.677078f, 10.677078f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				38f, 38f, 38f, 5f, 6f, 7f, 8f, 9f, 10f, 19.5f, 19.5f, 19.5f, 5f, 6f, 7f, 8f, 9f, 10f, 10.965857f, 10.965857f, 10.965857f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				39f, 39f, 39f, 5f, 6f, 7f, 8f, 9f, 10f, 20f, 20f, 20f, 5f, 6f, 7f, 8f, 9f, 10f, 11.254629f, 11.254629f, 11.254629f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				40f, 40f, 40f, 5f, 6f, 7f, 8f, 9f, 10f, 20.5f, 20.5f, 20.5f, 5f, 6f, 7f, 8f, 9f, 10f, 11.543396f, 11.543396f, 11.543396f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				41f, 41f, 41f, 5f, 6f, 7f, 8f, 9f, 10f, 21f, 21f, 21f, 5f, 6f, 7f, 8f, 9f, 10f, 11.83216f, 11.83216f, 11.83216f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				42f, 42f, 42f, 5f, 6f, 7f, 8f, 9f, 10f, 21.5f, 21.5f, 21.5f, 5f, 6f, 7f, 8f, 9f, 10f, 12.120918f, 12.120918f, 12.120918f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				43f, 43f, 43f, 5f, 6f, 7f, 8f, 9f, 10f, 22f, 22f, 22f, 5f, 6f, 7f, 8f, 9f, 10f, 12.409674f, 12.409674f, 12.409674f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				44f, 44f, 44f, 5f, 6f, 7f, 8f, 9f, 10f, 22.5f, 22.5f, 22.5f, 5f, 6f, 7f, 8f, 9f, 10f, 12.698425f, 12.698425f, 12.698425f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				45f, 45f, 45f, 5f, 6f, 7f, 8f, 9f, 10f, 23f, 23f, 23f, 5f, 6f, 7f, 8f, 9f, 10f, 12.987173f, 12.987173f, 12.987173f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				46f, 46f, 46f, 5f, 6f, 7f, 8f, 9f, 10f, 23.5f, 23.5f, 23.5f, 5f, 6f, 7f, 8f, 9f, 10f, 13.275918f, 13.275918f, 13.275918f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				47f, 47f, 47f, 5f, 6f, 7f, 8f, 9f, 10f, 24f, 24f, 24f, 5f, 6f, 7f, 8f, 9f, 10f, 13.56466f, 13.56466f, 13.56466f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				48f, 48f, 48f, 5f, 6f, 7f, 8f, 9f, 10f, 24.5f, 24.5f, 24.5f, 5f, 6f, 7f, 8f, 9f, 10f, 13.853399f, 13.853399f, 13.853399f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				49f, 49f, 49f, 5f, 6f, 7f, 8f, 9f, 10f, 25f, 25f, 25f, 5f, 6f, 7f, 8f, 9f, 10f, 14.142136f, 14.142136f, 14.142136f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				50f, 50f, 50f, 5f, 6f, 7f, 8f, 9f, 10f, 25.5f, 25.5f, 25.5f, 5f, 6f, 7f, 8f, 9f, 10f, 14.43087f, 14.43087f, 14.43087f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				51f, 51f, 51f, 5f, 6f, 7f, 8f, 9f, 10f, 26f, 26f, 26f, 5f, 6f, 7f, 8f, 9f, 10f, 14.719602f, 14.719602f, 14.719602f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				52f, 52f, 52f, 5f, 6f, 7f, 8f, 9f, 10f, 26.5f, 26.5f, 26.5f, 5f, 6f, 7f, 8f, 9f, 10f, 15.008331f, 15.008331f, 15.008331f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				53f, 53f, 53f, 5f, 6f, 7f, 8f, 9f, 10f, 27f, 27f, 27f, 5f, 6f, 7f, 8f, 9f, 10f, 15.297058f, 15.297058f, 15.297058f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				54f, 54f, 54f, 5f, 6f, 7f, 8f, 9f, 10f, 27.5f, 27.5f, 27.5f, 5f, 6f, 7f, 8f, 9f, 10f, 15.585784f, 15.585784f, 15.585784f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f,
				55f, 55f, 55f, 5f, 6f, 7f, 8f, 9f, 10f, 28f, 28f, 28f, 5f, 6f, 7f, 8f, 9f, 10f, 15.874508f, 15.874508f, 15.874508f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 1f, 2f, 3f, 4f, 5f
			),
			result["first"]?.data?.toFloatList()?.toFloatArray()
		)
		assertContentEquals(
			floatArrayOf(
				6f, 6f, 6f, 5f, 6f, 7f, 8f, 9f, 10f, 3.5f, 3.5f, 3.5f, 5f, 6f, 7f, 8f, 9f, 10f, 1.7078252f, 1.7078252f, 1.7078252f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				7f, 7f, 7f, 5f, 6f, 7f, 8f, 9f, 10f, 4f, 4f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 2f, 2f, 2f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				8f, 8f, 8f, 5f, 6f, 7f, 8f, 9f, 10f, 4.5f, 4.5f, 4.5f, 5f, 6f, 7f, 8f, 9f, 10f, 2.291288f, 2.291288f, 2.291288f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				9f, 9f, 9f, 5f, 6f, 7f, 8f, 9f, 10f, 5f, 5f, 5f, 5f, 6f, 7f, 8f, 9f, 10f, 2.5819888f, 2.5819888f, 2.5819888f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				10f, 10f, 10f, 5f, 6f, 7f, 8f, 9f, 10f, 5.5f, 5.5f, 5.5f, 5f, 6f, 7f, 8f, 9f, 10f, 2.8722813f, 2.8722813f, 2.8722813f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				11f, 11f, 11f, 5f, 6f, 7f, 8f, 9f, 10f, 6f, 6f, 6f, 5f, 6f, 7f, 8f, 9f, 10f, 3.1622777f, 3.1622777f, 3.1622777f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				12f, 12f, 12f, 5f, 6f, 7f, 8f, 9f, 10f, 6.5f, 6.5f, 6.5f, 5f, 6f, 7f, 8f, 9f, 10f, 3.4520526f, 3.4520526f, 3.4520526f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				13f, 13f, 13f, 5f, 6f, 7f, 8f, 9f, 10f, 7f, 7f, 7f, 5f, 6f, 7f, 8f, 9f, 10f, 3.7416575f, 3.7416575f, 3.7416575f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				14f, 14f, 14f, 5f, 6f, 7f, 8f, 9f, 10f, 7.5f, 7.5f, 7.5f, 5f, 6f, 7f, 8f, 9f, 10f, 4.031129f, 4.031129f, 4.031129f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				15f, 15f, 15f, 5f, 6f, 7f, 8f, 9f, 10f, 8f, 8f, 8f, 5f, 6f, 7f, 8f, 9f, 10f, 4.3204937f, 4.3204937f, 4.3204937f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				16f, 16f, 16f, 5f, 6f, 7f, 8f, 9f, 10f, 8.5f, 8.5f, 8.5f, 5f, 6f, 7f, 8f, 9f, 10f, 4.609772f, 4.609772f, 4.609772f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				17f, 17f, 17f, 5f, 6f, 7f, 8f, 9f, 10f, 9f, 9f, 9f, 5f, 6f, 7f, 8f, 9f, 10f, 4.8989797f, 4.8989797f, 4.8989797f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				18f, 18f, 18f, 5f, 6f, 7f, 8f, 9f, 10f, 9.5f, 9.5f, 9.5f, 5f, 6f, 7f, 8f, 9f, 10f, 5.1881275f, 5.1881275f, 5.1881275f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				19f, 19f, 19f, 5f, 6f, 7f, 8f, 9f, 10f, 10f, 10f, 10f, 5f, 6f, 7f, 8f, 9f, 10f, 5.477226f, 5.477226f, 5.477226f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				20f, 20f, 20f, 5f, 6f, 7f, 8f, 9f, 10f, 10.5f, 10.5f, 10.5f, 5f, 6f, 7f, 8f, 9f, 10f, 5.766281f, 5.766281f, 5.766281f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				21f, 21f, 21f, 5f, 6f, 7f, 8f, 9f, 10f, 11f, 11f, 11f, 5f, 6f, 7f, 8f, 9f, 10f, 6.0553007f, 6.0553007f, 6.0553007f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				22f, 22f, 22f, 5f, 6f, 7f, 8f, 9f, 10f, 11.5f, 11.5f, 11.5f, 5f, 6f, 7f, 8f, 9f, 10f, 6.344289f, 6.344289f, 6.344289f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				23f, 23f, 23f, 5f, 6f, 7f, 8f, 9f, 10f, 12f, 12f, 12f, 5f, 6f, 7f, 8f, 9f, 10f, 6.6332498f, 6.6332498f, 6.6332498f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				24f, 24f, 24f, 5f, 6f, 7f, 8f, 9f, 10f, 12.5f, 12.5f, 12.5f, 5f, 6f, 7f, 8f, 9f, 10f, 6.9221864f, 6.9221864f, 6.9221864f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				25f, 25f, 25f, 5f, 6f, 7f, 8f, 9f, 10f, 13f, 13f, 13f, 5f, 6f, 7f, 8f, 9f, 10f, 7.2111025f, 7.2111025f, 7.2111025f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				26f, 26f, 26f, 5f, 6f, 7f, 8f, 9f, 10f, 13.5f, 13.5f, 13.5f, 5f, 6f, 7f, 8f, 9f, 10f, 7.5f, 7.5f, 7.5f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				27f, 27f, 27f, 5f, 6f, 7f, 8f, 9f, 10f, 14f, 14f, 14f, 5f, 6f, 7f, 8f, 9f, 10f, 7.788881f, 7.788881f, 7.788881f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				28f, 28f, 28f, 5f, 6f, 7f, 8f, 9f, 10f, 14.5f, 14.5f, 14.5f, 5f, 6f, 7f, 8f, 9f, 10f, 8.077747f, 8.077747f, 8.077747f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				29f, 29f, 29f, 5f, 6f, 7f, 8f, 9f, 10f, 15f, 15f, 15f, 5f, 6f, 7f, 8f, 9f, 10f, 8.3666f, 8.3666f, 8.3666f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				30f, 30f, 30f, 5f, 6f, 7f, 8f, 9f, 10f, 15.5f, 15.5f, 15.5f, 5f, 6f, 7f, 8f, 9f, 10f, 8.655441f, 8.655441f, 8.655441f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				31f, 31f, 31f, 5f, 6f, 7f, 8f, 9f, 10f, 16f, 16f, 16f, 5f, 6f, 7f, 8f, 9f, 10f, 8.944272f, 8.944272f, 8.944272f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				32f, 32f, 32f, 5f, 6f, 7f, 8f, 9f, 10f, 16.5f, 16.5f, 16.5f, 5f, 6f, 7f, 8f, 9f, 10f, 9.233092f, 9.233092f, 9.233092f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				33f, 33f, 33f, 5f, 6f, 7f, 8f, 9f, 10f, 17f, 17f, 17f, 5f, 6f, 7f, 8f, 9f, 10f, 9.521905f, 9.521905f, 9.521905f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				34f, 34f, 34f, 5f, 6f, 7f, 8f, 9f, 10f, 17.5f, 17.5f, 17.5f, 5f, 6f, 7f, 8f, 9f, 10f, 9.810708f, 9.810708f, 9.810708f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				35f, 35f, 35f, 5f, 6f, 7f, 8f, 9f, 10f, 18f, 18f, 18f, 5f, 6f, 7f, 8f, 9f, 10f, 10.099504f, 10.099504f, 10.099504f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				36f, 36f, 36f, 5f, 6f, 7f, 8f, 9f, 10f, 18.5f, 18.5f, 18.5f, 5f, 6f, 7f, 8f, 9f, 10f, 10.388294f, 10.388294f, 10.388294f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				37f, 37f, 37f, 5f, 6f, 7f, 8f, 9f, 10f, 19f, 19f, 19f, 5f, 6f, 7f, 8f, 9f, 10f, 10.677078f, 10.677078f, 10.677078f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				38f, 38f, 38f, 5f, 6f, 7f, 8f, 9f, 10f, 19.5f, 19.5f, 19.5f, 5f, 6f, 7f, 8f, 9f, 10f, 10.965857f, 10.965857f, 10.965857f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				39f, 39f, 39f, 5f, 6f, 7f, 8f, 9f, 10f, 20f, 20f, 20f, 5f, 6f, 7f, 8f, 9f, 10f, 11.254629f, 11.254629f, 11.254629f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				40f, 40f, 40f, 5f, 6f, 7f, 8f, 9f, 10f, 20.5f, 20.5f, 20.5f, 5f, 6f, 7f, 8f, 9f, 10f, 11.543396f, 11.543396f, 11.543396f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				41f, 41f, 41f, 5f, 6f, 7f, 8f, 9f, 10f, 21f, 21f, 21f, 5f, 6f, 7f, 8f, 9f, 10f, 11.83216f, 11.83216f, 11.83216f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				42f, 42f, 42f, 5f, 6f, 7f, 8f, 9f, 10f, 21.5f, 21.5f, 21.5f, 5f, 6f, 7f, 8f, 9f, 10f, 12.120918f, 12.120918f, 12.120918f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				43f, 43f, 43f, 5f, 6f, 7f, 8f, 9f, 10f, 22f, 22f, 22f, 5f, 6f, 7f, 8f, 9f, 10f, 12.409674f, 12.409674f, 12.409674f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				44f, 44f, 44f, 5f, 6f, 7f, 8f, 9f, 10f, 22.5f, 22.5f, 22.5f, 5f, 6f, 7f, 8f, 9f, 10f, 12.698425f, 12.698425f, 12.698425f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				45f, 45f, 45f, 5f, 6f, 7f, 8f, 9f, 10f, 23f, 23f, 23f, 5f, 6f, 7f, 8f, 9f, 10f, 12.987173f, 12.987173f, 12.987173f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				46f, 46f, 46f, 5f, 6f, 7f, 8f, 9f, 10f, 23.5f, 23.5f, 23.5f, 5f, 6f, 7f, 8f, 9f, 10f, 13.275918f, 13.275918f, 13.275918f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				47f, 47f, 47f, 5f, 6f, 7f, 8f, 9f, 10f, 24f, 24f, 24f, 5f, 6f, 7f, 8f, 9f, 10f, 13.56466f, 13.56466f, 13.56466f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				48f, 48f, 48f, 5f, 6f, 7f, 8f, 9f, 10f, 24.5f, 24.5f, 24.5f, 5f, 6f, 7f, 8f, 9f, 10f, 13.853399f, 13.853399f, 13.853399f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				49f, 49f, 49f, 5f, 6f, 7f, 8f, 9f, 10f, 25f, 25f, 25f, 5f, 6f, 7f, 8f, 9f, 10f, 14.142136f, 14.142136f, 14.142136f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				50f, 50f, 50f, 5f, 6f, 7f, 8f, 9f, 10f, 25.5f, 25.5f, 25.5f, 5f, 6f, 7f, 8f, 9f, 10f, 14.43087f, 14.43087f, 14.43087f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				51f, 51f, 51f, 5f, 6f, 7f, 8f, 9f, 10f, 26f, 26f, 26f, 5f, 6f, 7f, 8f, 9f, 10f, 14.719602f, 14.719602f, 14.719602f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				52f, 52f, 52f, 5f, 6f, 7f, 8f, 9f, 10f, 26.5f, 26.5f, 26.5f, 5f, 6f, 7f, 8f, 9f, 10f, 15.008331f, 15.008331f, 15.008331f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				53f, 53f, 53f, 5f, 6f, 7f, 8f, 9f, 10f, 27f, 27f, 27f, 5f, 6f, 7f, 8f, 9f, 10f, 15.297058f, 15.297058f, 15.297058f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				54f, 54f, 54f, 5f, 6f, 7f, 8f, 9f, 10f, 27.5f, 27.5f, 27.5f, 5f, 6f, 7f, 8f, 9f, 10f, 15.585784f, 15.585784f, 15.585784f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f,
				55f, 55f, 55f, 5f, 6f, 7f, 8f, 9f, 10f, 28f, 28f, 28f, 5f, 6f, 7f, 8f, 9f, 10f, 15.874508f, 15.874508f, 15.874508f, 0f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f, 0f, 1f, 12f, 1f, 13f, 1f, 0f, 1f, 0f, 17f, 17f, 0f, 1f, 6f, 7f, 8f, 9f, 10f
			),
			result["second"]?.data?.toFloatList()?.toFloatArray()
		)
	}
}