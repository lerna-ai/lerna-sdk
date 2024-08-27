package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.MergeInputData
import ai.lerna.multiplatform.ModelData
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MLInferenceTest {
	private lateinit var mlInference: MLInference
	private lateinit var modelData: ModelData
	private lateinit var inferenceData: MergeInputData

	@BeforeTest
	fun setUp() {
		mlInference = MLInference(0.5f)
		modelData = ModelData(50)
		inferenceData = MergeInputData(modelData, 5)
	}

	@Test
	fun `predictLabelFrom1Line1Item_failure_without_weights`() {
		// Given
		manipulateModelData()
		val testFeatures = mk.ndarray(listOf(modelData.toArray().toFloatArray()).toTypedArray())
		// When
		val result = mlInference.predictLabelFrom1Line1Item(testFeatures)
		// Then
		assertEquals("failure", result)
	}

	@Test
	fun `predictLabelFrom1Line1Item_with_zeros_model_data`() {
		// Given
		val testFeatures = mk.ndarray(listOf(modelData.toArray().toFloatArray()).toTypedArray())
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()
		globalTrainingWeightsItem.weights = mapOf(
			Pair("Comment", mk.ndarray(listOf(floatArrayOf(-0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f)).toTypedArray()).reshape(178, 1)),
			Pair("Like", mk.ndarray(listOf(floatArrayOf(-1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f)).toTypedArray()).reshape(178, 1))
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		// When
		mlInference.setWeights(globalTrainingWeightsItem)
		val result = mlInference.predictLabelFrom1Line1Item(testFeatures)
		// Then
		assertEquals("Comment", result)
	}

	@Test
	fun `predictLabelFrom1Line1Item`() {
		// Given
		manipulateModelData()
		val testFeatures = mk.ndarray(listOf(modelData.toArray().toFloatArray()).toTypedArray())
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()
		globalTrainingWeightsItem.weights = mapOf(
			Pair("Comment", mk.ndarray(listOf(floatArrayOf(-0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f)).toTypedArray()).reshape(178, 1)),
			Pair("Like", mk.ndarray(listOf(floatArrayOf(-1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f)).toTypedArray()).reshape(178, 1))
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		// When
		mlInference.setWeights(globalTrainingWeightsItem)
		val result = mlInference.predictLabelFrom1Line1Item(testFeatures)
		// Then
		assertEquals("Like", result)
	}

	@Test
	fun `predictLabelFrom1Line1Item_with_custom_features`() {
		// Given
		manipulateModelData()
		modelData.setupCustomFeatureSize(5)
		modelData.updateCustomFeatures(floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f))
		val testFeatures = mk.ndarray(listOf(modelData.toArray().toFloatArray()).toTypedArray())
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()
		globalTrainingWeightsItem.weights = mapOf(
			Pair("Comment", mk.ndarray(listOf(floatArrayOf(-0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f)).toTypedArray()).reshape(183, 1)),
			Pair("Like", mk.ndarray(listOf(floatArrayOf(-1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f)).toTypedArray()).reshape(183, 1))
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		mlInference.setWeights(globalTrainingWeightsItem)
		// When
		val result = mlInference.predictLabelFrom1Line1Item(testFeatures)
		// Then
		assertEquals("Like", result)
	}

	@Test
	fun `predictLabelScoreMulLinesMulItems_class_not_exists`() {
		// Given
		manipulateModelData()
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
		val thetaName = "Dislike"
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()
		globalTrainingWeightsItem.weights = mapOf(
			Pair("Comment", mk.ndarray(listOf(floatArrayOf(-0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, 1.4313f, -1.7988f, 0.2193f, 0.5958f, -0.7268f)).toTypedArray()).reshape(51, 1)),
			Pair("Like", mk.ndarray(listOf(floatArrayOf(-1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.1816f, 1.2985f, -0.8554f, -0.2055f, -0.0658f)).toTypedArray()).reshape(51, 1))
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		mlInference.setWeights(globalTrainingWeightsItem)
		// When
		val result = mlInference.predictLabelScoreMulLinesMulItems(testFeatures, thetaName)
		// Then
		assertNull(result)
	}

	@Test
	fun `predictLabelScoreMulLinesMulItems`() {
		// Given
		manipulateModelData()
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
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()
		globalTrainingWeightsItem.weights = mapOf(
			Pair("Comment", mk.ndarray(listOf(floatArrayOf(-0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, 1.4313f, -1.7988f, 0.2193f, 0.5958f, -0.7268f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, 1.4313f, -1.7988f, 0.2193f, 0.5958f, -0.7268f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, 1.4313f, -1.7988f, 0.2193f, 0.5958f, -0.7268f, -0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f)).toTypedArray()).reshape(183, 1)),
			Pair("Like", mk.ndarray(listOf(floatArrayOf(-1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.1816f, 1.2985f, -0.8554f, -0.2055f, -0.0658f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.1816f, 1.2985f, -0.8554f, -0.2055f, -0.0658f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.1816f, 1.2985f, -0.8554f, -0.2055f, -0.0658f, -1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f)).toTypedArray()).reshape(183, 1))
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		mlInference.setWeights(globalTrainingWeightsItem)
		// When
		val result = mlInference.predictLabelScoreMulLinesMulItems(testFeatures, thetaName)
		// Then
		assertEquals(3, result?.size)
		assertEquals(0.93310934f/3, result?.get("first"))
		assertEquals(3.9330442E-4f/3, result?.get("second"))
		assertEquals(1.1097612E-8f/3, result?.get("third"))
	}

	@Test
	fun `predictLabelScoreMulLinesMulItems_data4Inference_empty`() {
		// Given
		manipulateModelData()
		modelData.setHistory()
		modelData.setHistory()
		modelData.setHistory()
		modelData.setupCustomFeatureSize(5)
		modelData.updateCustomFeatures(floatArrayOf(0.1f, 0.2f, 0.3f, 0.4f, 0.5f))
		val data4Inference = mutableMapOf<String, FloatArray>()
		val testFeatures = inferenceData.getMergedInputDataHistory(data4Inference)
		val thetaName = "Comment"
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()
		globalTrainingWeightsItem.weights = mapOf(
			Pair("Comment", mk.ndarray(listOf(floatArrayOf(-0.4156f, -1.2238f, 0.4486f, -1.1574f, 1.0517f, 0.6329f, -0.5608f, 0.0145f, -0.0791f, 0.2121f, -0.8953f, 0.2108f, 0.5131f, -0.1825f, -0.0756f, 1.8212f, -0.6815f, 0.1803f, 0.0780f, -0.8205f, 0.4557f, 1.6921f, -0.3133f, -1.8275f, -0.8802f, -0.4808f, -0.3227f, 0.0013f, -1.2962f, -1.0472f, -0.3855f, 1.0170f, -0.2641f, -0.3861f, -0.5292f, -0.1152f, 0.0938f, -0.1070f, -0.8860f, -0.0360f, 1.9614f, -1.1113f, -0.6554f, 0.6648f, 0.4622f, 1.6860f, 1.4313f, -1.7988f, 0.2193f, 0.5958f, -0.7268f)).toTypedArray()).reshape(51, 1)),
			Pair("Like", mk.ndarray(listOf(floatArrayOf(-1.2667f, 0.1534f, -1.3488f, -0.6636f, 0.6352f, 0.0947f, 0.8571f, -0.3698f, -0.6165f, 1.6966f, -1.3589f, 0.5566f, -0.7810f, -0.0150f, 1.4092f, -0.1786f, 0.2859f, -0.7151f, 0.2185f, -0.5758f, -0.3234f, 0.2182f, -2.5444f, -1.2448f, 0.5637f, 0.8383f, 0.8408f, 0.1179f, 0.0190f, -1.1387f, 1.0532f, -0.0253f, 0.8879f, -0.8804f, 2.0893f, 1.4412f, 1.8306f, 1.6181f, 0.8238f, 0.5846f, -0.4266f, 0.0828f, 1.1133f, -0.0257f, -0.4313f, 0.2156f, -1.1816f, 1.2985f, -0.8554f, -0.2055f, -0.0658f)).toTypedArray()).reshape(51, 1))
		)
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		mlInference.setWeights(globalTrainingWeightsItem)
		// When
		val result = mlInference.predictLabelScoreMulLinesMulItems(testFeatures, thetaName)
		// Then
		assertEquals(0, result?.size)
	}

	@Test
	fun `predictLabelScoreMulLinesMulItems_weights_empty`() {
		// Given
		manipulateModelData()
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
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()
		globalTrainingWeightsItem.weights = mapOf()
		globalTrainingWeightsItem.mlId = 12
		globalTrainingWeightsItem.mlName = "Content"
		globalTrainingWeightsItem.accuracy = 0f
		mlInference.setWeights(globalTrainingWeightsItem)
		// When
		val result = mlInference.predictLabelScoreMulLinesMulItems(testFeatures, thetaName)
		// Then
		assertNull(result)
	}

	@Test
	fun `predictLabelScoreMulLinesMulItems_no_weights`() {
		// Given
		manipulateModelData()
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
		// When
		val result = mlInference.predictLabelScoreMulLinesMulItems(testFeatures, thetaName)
		// Then
		assertNull(result)
	}

	private fun manipulateModelData() {
		modelData.setLinAcceleration(2F, 3F, 4F)
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
	}
}