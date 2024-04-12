package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.MergeInputData
import ai.lerna.multiplatform.ModelData
import ai.lerna.multiplatform.service.advancedML.SimpleExample
import ai.lerna.multiplatform.service.dto.AdvancedMLItem
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.first
import org.jetbrains.kotlinx.multik.ndarray.operations.forEach
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MLInferenceNewModelTest {
	private lateinit var mlInference: MLInferenceNewModel
	private lateinit var modelData: ModelData
	private lateinit var inferenceData: MergeInputData

	@BeforeTest
	fun setUp() {
		mlInference = MLInferenceNewModel()
		modelData = ModelData(50)
		inferenceData = MergeInputData(modelData, 5)
	}

	@Test
	fun `predictLabelFrom1Line1Item_failure_without_weights`() {
		// Given
		//manipulateModelData()
		val testFeatures = floatArrayOf(1.0f,6.7f,3.0f,5.0f,1.7f)//.toArray().take(5)

		val testExample =
			SimpleExample(
				//train_categIndexes[it],
				null,
				//train_numIndexes[it], train_numValues[it],
				null,null,
				//listOf(train_multiHot_indexes[it]), listOf(train_multiHot_values[it]),
				null, null,
				testFeatures.map { it }.toFloatArray(),
				null
				//trainLabels[it]
			)


		// When
		val result = mlInference.predictLabelFrom1Line1Item(testExample)
		// Then
		assertEquals("failure", result)
	}

	@Test
	fun `predictLabelFrom1Line1Item_with_zeros_model_data`() {
		// Given
		val testFeatures = floatArrayOf(0.0f,0.0f,0.0f,0.0f)

		val testExample =
			SimpleExample(
				//train_categIndexes[it],
				null,
				//train_numIndexes[it], train_numValues[it],
				null,null,
				//listOf(train_multiHot_indexes[it]), listOf(train_multiHot_values[it]),
				null, null,
				testFeatures.map { it }.toFloatArray(),
				null
				//trainLabels[it]
			)
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()

		val temp1 = arrayOf(floatArrayOf(0.28293687f, -0.60018724f, -0.24564528f, -0.39238974f),
			floatArrayOf(0.09881812f, 0.15511145f, -0.3954835f, -0.3896694f),
			floatArrayOf(-0.081900835f, 0.022636853f, 0.10433076f, 0.865149f),
			floatArrayOf(0.12928279f, 0.045945443f, -0.29600155f, -0.5850915f),
			floatArrayOf(-0.7017922f, -0.3070385f, -0.38738438f, -0.38456556f),
			floatArrayOf(-0.24922022f, 0.080352575f, 0.5312609f, 0.25138387f),
			floatArrayOf(0.21058288f, 0.18980116f, -0.45036379f, -1.0610094f),
			floatArrayOf(-0.30883506f, 0.2953443f, 0.22071537f, 1.0275143f),
			floatArrayOf(0.010458527f, 0.61703485f, -0.12099516f, 0.83198947f),
			floatArrayOf(0.14396705f, -0.1788505f, -0.04162597f, 0.7813448f),
			floatArrayOf(0.089264095f, 0.23542914f, -0.4672568f, -0.30023864f),
			floatArrayOf(-0.2874573f, -0.058466293f, 0.58064735f, 0.82717526f),
			floatArrayOf(-0.0063175745f, -0.4512875f, 0.5320778f, 0.25277638f),
			floatArrayOf(-0.17440401f, 0.29698434f, 0.13254249f, 0.7686539f),
			floatArrayOf(-0.40453136f, -0.485504f, -0.28021175f, 0.41679356f),
			floatArrayOf(-0.301117f, 0.01747038f, 0.5223835f, 0.8911013f))

		val temp2 = arrayOf(floatArrayOf(0.39053836f),
			floatArrayOf(0.22795086f),
			floatArrayOf(-0.1747529f),
			floatArrayOf(0.20733126f),
			floatArrayOf(-0.77444816f),
			floatArrayOf(-0.35685515f),
			floatArrayOf(0.34597397f),
			floatArrayOf(-0.45244095f),
			floatArrayOf(-0.039374087f),
			floatArrayOf(0.06852166f),
			floatArrayOf(0.18864061f),
			floatArrayOf(-0.36729428f),
			floatArrayOf(-0.083617605f),
			floatArrayOf(-0.26311973f),
			floatArrayOf(-0.49365738f),
			floatArrayOf(-0.38360053f))

		val temp3 = arrayOf(floatArrayOf(0.41102463f, 0.2934277f, -0.54024756f, 0.2876497f, -0.37097135f, -0.80739015f, 0.58236825f, -0.85408914f, -0.3172801f, -0.23850179f, 0.31112424f, -0.30624193f, -0.38534912f, -0.55441946f, -0.32359248f, -0.34786958f))

		val temp4 = arrayOf(floatArrayOf(1.405784f))

		val advancedMLmodel1 = AdvancedMLItem()
		advancedMLmodel1.sensors = listOf(Pair(listOf(mk.ndarray(temp1)),listOf(mk.ndarray(temp2))))
		advancedMLmodel1.lastlayer = listOf(Pair(listOf(mk.ndarray(temp3)),listOf(mk.ndarray(temp4))))


		val temp5 = arrayOf(floatArrayOf(0.32280427f, -0.74212754f, 0.041894704f, -0.056007933f),
			floatArrayOf(0.13041314f, -0.08284514f, 0.08000189f, 0.22108027f),
			floatArrayOf(-0.048534222f, 0.2718297f, -0.19723321f, 0.48610875f),
			floatArrayOf(0.09713495f, -0.16174112f, -0.07333343f, -0.3109811f),
			floatArrayOf(-0.17190433f, 0.41719598f, -0.29963312f, -0.5081868f),
			floatArrayOf(-0.21468952f, 0.33534673f, 0.22336203f, -0.1350819f),
			floatArrayOf(0.3222432f, 0.05549968f, 0.017726578f, -0.52839977f),
			floatArrayOf(-0.2886347f, 0.5793697f, -0.18101127f, 0.5159879f),
			floatArrayOf(-0.087542765f, 0.6603381f, -0.36643428f, 0.583144f),
			floatArrayOf(0.20065024f, 0.09789965f, -0.33513272f, 0.39570123f),
			floatArrayOf(0.10494686f, 0.039165616f, -0.13155013f, 0.10798334f),
			floatArrayOf(0.16349413f, 0.57220185f, 0.46927923f, 0.30711746f),
			floatArrayOf(0.18728882f, -0.03225266f, 0.27123317f, -0.15368906f),
			floatArrayOf(-0.15862276f, 0.5086391f, -0.13350114f, 0.4479435f),
			floatArrayOf(-0.07890555f, 0.08536938f, -0.42940268f, 0.08850024f),
			floatArrayOf(0.096114814f, 0.6005595f, 0.3683523f, 0.3827897f))

		val temp6 = arrayOf(floatArrayOf(0.3591389f),
			floatArrayOf(0.14375731f),
			floatArrayOf(-0.0577123f),
			floatArrayOf(0.10108796f),
			floatArrayOf(-0.17321983f),
			floatArrayOf(-0.23616117f),
			floatArrayOf(0.363372f),
			floatArrayOf(-0.32391f),
			floatArrayOf(-0.09379754f),
			floatArrayOf(0.20814496f),
			floatArrayOf(0.12090685f),
			floatArrayOf(0.16879699f),
			floatArrayOf(0.21330854f),
			floatArrayOf(-0.17225175f),
			floatArrayOf(-0.07287551f),
			floatArrayOf(0.1012205f))

		val temp7 = arrayOf(floatArrayOf(0.43311465f, 0.20825443f, -0.50178003f, 0.097225346f, -1.0966033f, -0.67858416f, 0.414921f, -0.88165766f, -0.38476074f, -0.23564076f, 0.15219902f, -0.22405466f, -0.22715905f, -0.5645844f, -0.8016501f, -0.27251887f))

		val temp8 = arrayOf(floatArrayOf(1.3699387f))

		val advancedMLmodel2 = AdvancedMLItem()
		advancedMLmodel2.sensors = listOf(Pair(listOf(mk.ndarray(temp5)),listOf(mk.ndarray(temp6))))
		advancedMLmodel2.lastlayer = listOf(Pair(listOf(mk.ndarray(temp7)),listOf(mk.ndarray(temp8))))

		globalTrainingWeightsItem.weightsMultiKv2 = mapOf(
			Pair("Iris-setosa", advancedMLmodel1),
			Pair("Iris-versicolor", advancedMLmodel2)
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f

		// When
		mlInference.setWeights(globalTrainingWeightsItem)


		val result = mlInference.predictLabelFrom1Line1Item(testExample)
		// Then
		assertEquals("Iris-setosa", result)
	}

	@Test
	fun `predictLabelFrom1Line1Item`() {
		// Given
		val testFeatures = floatArrayOf(5.7f,2.9f,4.2f,1.3f)

		val testExample =
			SimpleExample(
				//train_categIndexes[it],
				null,
				//train_numIndexes[it], train_numValues[it],
				null,null,
				//listOf(train_multiHot_indexes[it]), listOf(train_multiHot_values[it]),
				null, null,
				testFeatures.map { it }.toFloatArray(),
				null
				//trainLabels[it]
			)
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()

		val temp1 = arrayOf(floatArrayOf(0.28293687f, -0.60018724f, -0.24564528f, -0.39238974f),
			floatArrayOf(0.09881812f, 0.15511145f, -0.3954835f, -0.3896694f),
			floatArrayOf(-0.081900835f, 0.022636853f, 0.10433076f, 0.865149f),
			floatArrayOf(0.12928279f, 0.045945443f, -0.29600155f, -0.5850915f),
			floatArrayOf(-0.7017922f, -0.3070385f, -0.38738438f, -0.38456556f),
			floatArrayOf(-0.24922022f, 0.080352575f, 0.5312609f, 0.25138387f),
			floatArrayOf(0.21058288f, 0.18980116f, -0.45036379f, -1.0610094f),
			floatArrayOf(-0.30883506f, 0.2953443f, 0.22071537f, 1.0275143f),
			floatArrayOf(0.010458527f, 0.61703485f, -0.12099516f, 0.83198947f),
			floatArrayOf(0.14396705f, -0.1788505f, -0.04162597f, 0.7813448f),
			floatArrayOf(0.089264095f, 0.23542914f, -0.4672568f, -0.30023864f),
			floatArrayOf(-0.2874573f, -0.058466293f, 0.58064735f, 0.82717526f),
			floatArrayOf(-0.0063175745f, -0.4512875f, 0.5320778f, 0.25277638f),
			floatArrayOf(-0.17440401f, 0.29698434f, 0.13254249f, 0.7686539f),
			floatArrayOf(-0.40453136f, -0.485504f, -0.28021175f, 0.41679356f),
			floatArrayOf(-0.301117f, 0.01747038f, 0.5223835f, 0.8911013f))

		val temp2 = arrayOf(floatArrayOf(0.39053836f),
			floatArrayOf(0.22795086f),
			floatArrayOf(-0.1747529f),
			floatArrayOf(0.20733126f),
			floatArrayOf(-0.77444816f),
			floatArrayOf(-0.35685515f),
			floatArrayOf(0.34597397f),
			floatArrayOf(-0.45244095f),
			floatArrayOf(-0.039374087f),
			floatArrayOf(0.06852166f),
			floatArrayOf(0.18864061f),
			floatArrayOf(-0.36729428f),
			floatArrayOf(-0.083617605f),
			floatArrayOf(-0.26311973f),
			floatArrayOf(-0.49365738f),
			floatArrayOf(-0.38360053f))

		val temp3 = arrayOf(floatArrayOf(0.41102463f, 0.2934277f, -0.54024756f, 0.2876497f, -0.37097135f, -0.80739015f, 0.58236825f, -0.85408914f, -0.3172801f, -0.23850179f, 0.31112424f, -0.30624193f, -0.38534912f, -0.55441946f, -0.32359248f, -0.34786958f))

		val temp4 = arrayOf(floatArrayOf(1.405784f))

		val advancedMLmodel1 = AdvancedMLItem()
		advancedMLmodel1.sensors = listOf(Pair(listOf(mk.ndarray(temp1)),listOf(mk.ndarray(temp2))))
		advancedMLmodel1.lastlayer = listOf(Pair(listOf(mk.ndarray(temp3)),listOf(mk.ndarray(temp4))))


		val temp5 = arrayOf(floatArrayOf(0.32280427f, -0.74212754f, 0.041894704f, -0.056007933f),
			floatArrayOf(0.13041314f, -0.08284514f, 0.08000189f, 0.22108027f),
			floatArrayOf(-0.048534222f, 0.2718297f, -0.19723321f, 0.48610875f),
			floatArrayOf(0.09713495f, -0.16174112f, -0.07333343f, -0.3109811f),
			floatArrayOf(-0.17190433f, 0.41719598f, -0.29963312f, -0.5081868f),
			floatArrayOf(-0.21468952f, 0.33534673f, 0.22336203f, -0.1350819f),
			floatArrayOf(0.3222432f, 0.05549968f, 0.017726578f, -0.52839977f),
			floatArrayOf(-0.2886347f, 0.5793697f, -0.18101127f, 0.5159879f),
			floatArrayOf(-0.087542765f, 0.6603381f, -0.36643428f, 0.583144f),
			floatArrayOf(0.20065024f, 0.09789965f, -0.33513272f, 0.39570123f),
			floatArrayOf(0.10494686f, 0.039165616f, -0.13155013f, 0.10798334f),
			floatArrayOf(0.16349413f, 0.57220185f, 0.46927923f, 0.30711746f),
			floatArrayOf(0.18728882f, -0.03225266f, 0.27123317f, -0.15368906f),
			floatArrayOf(-0.15862276f, 0.5086391f, -0.13350114f, 0.4479435f),
			floatArrayOf(-0.07890555f, 0.08536938f, -0.42940268f, 0.08850024f),
			floatArrayOf(0.096114814f, 0.6005595f, 0.3683523f, 0.3827897f))

		val temp6 = arrayOf(floatArrayOf(0.3591389f),
			floatArrayOf(0.14375731f),
			floatArrayOf(-0.0577123f),
			floatArrayOf(0.10108796f),
			floatArrayOf(-0.17321983f),
			floatArrayOf(-0.23616117f),
			floatArrayOf(0.363372f),
			floatArrayOf(-0.32391f),
			floatArrayOf(-0.09379754f),
			floatArrayOf(0.20814496f),
			floatArrayOf(0.12090685f),
			floatArrayOf(0.16879699f),
			floatArrayOf(0.21330854f),
			floatArrayOf(-0.17225175f),
			floatArrayOf(-0.07287551f),
			floatArrayOf(0.1012205f))

		val temp7 = arrayOf(floatArrayOf(0.43311465f, 0.20825443f, -0.50178003f, 0.097225346f, -1.0966033f, -0.67858416f, 0.414921f, -0.88165766f, -0.38476074f, -0.23564076f, 0.15219902f, -0.22405466f, -0.22715905f, -0.5645844f, -0.8016501f, -0.27251887f))

		val temp8 = arrayOf(floatArrayOf(1.3699387f))

		val advancedMLmodel2 = AdvancedMLItem()
		advancedMLmodel2.sensors = listOf(Pair(listOf(mk.ndarray(temp5)),listOf(mk.ndarray(temp6))))
		advancedMLmodel2.lastlayer = listOf(Pair(listOf(mk.ndarray(temp7)),listOf(mk.ndarray(temp8))))

		globalTrainingWeightsItem.weightsMultiKv2 = mapOf(
			Pair("Iris-setosa", advancedMLmodel1),
			Pair("Iris-versicolor", advancedMLmodel2)
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		// When
		mlInference.setWeights(globalTrainingWeightsItem)

		val result = mlInference.predictLabelFrom1Line1Item(testExample)
		// Then
		assertEquals("Iris-versicolor", result)
	}




	@Test
	fun `predictLabelScoreMulLinesMulItems`() {
		// Given


		val data4Inference = mutableMapOf<String, FloatArray>()
		data4Inference["first"] = floatArrayOf(5.7f,2.9f,4.2f,1.3f)
		data4Inference["second"] = floatArrayOf(4.6f,3.2f,1.4f,0.2f)
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()

//		val testFeatures:MutableMap<String, D2Array<Float>>
//		testFeatures.put("first", mk.ndarray(arrayOf(data4Inference["first"])))
//		testFeatures.put("second", mk.ndarray(data4Inference["second"]))

		val thetaName = "Iris-versicolor"
		val test = mutableMapOf<String, List<SimpleExample>>()
		for (testFeature in data4Inference) {
			val testExamples: MutableList<SimpleExample> = mutableListOf()
			for(i in 0..<testFeature.value.size) {
				testExamples.add(SimpleExample(
					//test_categIndexes[it],
					null,
					//test_numIndexes[it], test_numValues[it],
					null,null,
					//listOf(test_multiHot_indexes[it]), listOf(test_multiHot_values[it]),
					null, null,
					testFeature.value.map { it }.toFloatArray(),
					null
					//testLabels[it]
				))
			}
			test[testFeature.key] = testExamples
		}


		val temp1 = arrayOf(floatArrayOf(0.28293687f, -0.60018724f, -0.24564528f, -0.39238974f),
			floatArrayOf(0.09881812f, 0.15511145f, -0.3954835f, -0.3896694f),
			floatArrayOf(-0.081900835f, 0.022636853f, 0.10433076f, 0.865149f),
			floatArrayOf(0.12928279f, 0.045945443f, -0.29600155f, -0.5850915f),
			floatArrayOf(-0.7017922f, -0.3070385f, -0.38738438f, -0.38456556f),
			floatArrayOf(-0.24922022f, 0.080352575f, 0.5312609f, 0.25138387f),
			floatArrayOf(0.21058288f, 0.18980116f, -0.45036379f, -1.0610094f),
			floatArrayOf(-0.30883506f, 0.2953443f, 0.22071537f, 1.0275143f),
			floatArrayOf(0.010458527f, 0.61703485f, -0.12099516f, 0.83198947f),
			floatArrayOf(0.14396705f, -0.1788505f, -0.04162597f, 0.7813448f),
			floatArrayOf(0.089264095f, 0.23542914f, -0.4672568f, -0.30023864f),
			floatArrayOf(-0.2874573f, -0.058466293f, 0.58064735f, 0.82717526f),
			floatArrayOf(-0.0063175745f, -0.4512875f, 0.5320778f, 0.25277638f),
			floatArrayOf(-0.17440401f, 0.29698434f, 0.13254249f, 0.7686539f),
			floatArrayOf(-0.40453136f, -0.485504f, -0.28021175f, 0.41679356f),
			floatArrayOf(-0.301117f, 0.01747038f, 0.5223835f, 0.8911013f))

		val temp2 = arrayOf(floatArrayOf(0.39053836f),
			floatArrayOf(0.22795086f),
			floatArrayOf(-0.1747529f),
			floatArrayOf(0.20733126f),
			floatArrayOf(-0.77444816f),
			floatArrayOf(-0.35685515f),
			floatArrayOf(0.34597397f),
			floatArrayOf(-0.45244095f),
			floatArrayOf(-0.039374087f),
			floatArrayOf(0.06852166f),
			floatArrayOf(0.18864061f),
			floatArrayOf(-0.36729428f),
			floatArrayOf(-0.083617605f),
			floatArrayOf(-0.26311973f),
			floatArrayOf(-0.49365738f),
			floatArrayOf(-0.38360053f))

		val temp3 = arrayOf(floatArrayOf(0.41102463f, 0.2934277f, -0.54024756f, 0.2876497f, -0.37097135f, -0.80739015f, 0.58236825f, -0.85408914f, -0.3172801f, -0.23850179f, 0.31112424f, -0.30624193f, -0.38534912f, -0.55441946f, -0.32359248f, -0.34786958f))

		val temp4 = arrayOf(floatArrayOf(1.405784f))

		val advancedMLmodel1 = AdvancedMLItem()
		advancedMLmodel1.sensors = listOf(Pair(listOf(mk.ndarray(temp1)),listOf(mk.ndarray(temp2))))
		advancedMLmodel1.lastlayer = listOf(Pair(listOf(mk.ndarray(temp3)),listOf(mk.ndarray(temp4))))


		val temp5 = arrayOf(floatArrayOf(0.32280427f, -0.74212754f, 0.041894704f, -0.056007933f),
			floatArrayOf(0.13041314f, -0.08284514f, 0.08000189f, 0.22108027f),
			floatArrayOf(-0.048534222f, 0.2718297f, -0.19723321f, 0.48610875f),
			floatArrayOf(0.09713495f, -0.16174112f, -0.07333343f, -0.3109811f),
			floatArrayOf(-0.17190433f, 0.41719598f, -0.29963312f, -0.5081868f),
			floatArrayOf(-0.21468952f, 0.33534673f, 0.22336203f, -0.1350819f),
			floatArrayOf(0.3222432f, 0.05549968f, 0.017726578f, -0.52839977f),
			floatArrayOf(-0.2886347f, 0.5793697f, -0.18101127f, 0.5159879f),
			floatArrayOf(-0.087542765f, 0.6603381f, -0.36643428f, 0.583144f),
			floatArrayOf(0.20065024f, 0.09789965f, -0.33513272f, 0.39570123f),
			floatArrayOf(0.10494686f, 0.039165616f, -0.13155013f, 0.10798334f),
			floatArrayOf(0.16349413f, 0.57220185f, 0.46927923f, 0.30711746f),
			floatArrayOf(0.18728882f, -0.03225266f, 0.27123317f, -0.15368906f),
			floatArrayOf(-0.15862276f, 0.5086391f, -0.13350114f, 0.4479435f),
			floatArrayOf(-0.07890555f, 0.08536938f, -0.42940268f, 0.08850024f),
			floatArrayOf(0.096114814f, 0.6005595f, 0.3683523f, 0.3827897f))

		val temp6 = arrayOf(floatArrayOf(0.3591389f),
			floatArrayOf(0.14375731f),
			floatArrayOf(-0.0577123f),
			floatArrayOf(0.10108796f),
			floatArrayOf(-0.17321983f),
			floatArrayOf(-0.23616117f),
			floatArrayOf(0.363372f),
			floatArrayOf(-0.32391f),
			floatArrayOf(-0.09379754f),
			floatArrayOf(0.20814496f),
			floatArrayOf(0.12090685f),
			floatArrayOf(0.16879699f),
			floatArrayOf(0.21330854f),
			floatArrayOf(-0.17225175f),
			floatArrayOf(-0.07287551f),
			floatArrayOf(0.1012205f))

		val temp7 = arrayOf(floatArrayOf(0.43311465f, 0.20825443f, -0.50178003f, 0.097225346f, -1.0966033f, -0.67858416f, 0.414921f, -0.88165766f, -0.38476074f, -0.23564076f, 0.15219902f, -0.22405466f, -0.22715905f, -0.5645844f, -0.8016501f, -0.27251887f))

		val temp8 = arrayOf(floatArrayOf(1.3699387f))

		val advancedMLmodel2 = AdvancedMLItem()
		advancedMLmodel2.sensors = listOf(Pair(listOf(mk.ndarray(temp5)),listOf(mk.ndarray(temp6))))
		advancedMLmodel2.lastlayer = listOf(Pair(listOf(mk.ndarray(temp7)),listOf(mk.ndarray(temp8))))

		globalTrainingWeightsItem.weightsMultiKv2 = mapOf(
			Pair("Iris-setosa", advancedMLmodel1),
			Pair("Iris-versicolor", advancedMLmodel2)
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		// When
		mlInference.setWeights(globalTrainingWeightsItem)

		// When
		val result = mlInference.predictLabelScoreMulLinesMulItems(test, thetaName)
		// Then
		assertEquals(2, result?.size)
		assertEquals(1.5430856f, result?.get("first"))
		assertEquals(0.9248515f, result?.get("second"))

	}


	@Test
	fun `predictLabelScoreMulLinesMulItems_no_weights`() {
		// Given
		//manipulateModelData()
		modelData.setHistory()
		modelData.setHistory()
		modelData.setHistory()
		modelData.setupCustomFeatureSize(5)
		modelData.updateCustomFeatures(floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f))
		val data4Inference = mutableMapOf<String, FloatArray>()
		data4Inference["first"] = floatArrayOf(1f, 2f, 3f, 4f, 5f)
		data4Inference["second"] = floatArrayOf(6f, 7f, 8f, 9f, 10f)
		data4Inference["third"] = floatArrayOf(11f, 12f, 13f, 14f, 15f)
		val testFeatures = inferenceData.getMergedInputDataHistory(data4Inference)
		val thetaName = "Comment"
		val test = mutableMapOf<String, List<SimpleExample>>()
		for (testFeature in testFeatures) {
			val testExamples: MutableList<SimpleExample> = mutableListOf()
			for(i in 0..<testFeature.value.shape[0]) {
				testExamples.add(SimpleExample(
					//test_categIndexes[it],
					null,
					//test_numIndexes[it], test_numValues[it],
					null,null,
					//listOf(test_multiHot_indexes[it]), listOf(test_multiHot_values[it]),
					null, null,
					testFeature.value[i].toFloatArray().map { it }.toFloatArray(),
					null
					//testLabels[it]
				))
			}
			test[testFeature.key] = testExamples
		}
		// When
		val result = mlInference.predictLabelScoreMulLinesMulItems(test, thetaName)
		// Then
		assertNull(result)
	}


}