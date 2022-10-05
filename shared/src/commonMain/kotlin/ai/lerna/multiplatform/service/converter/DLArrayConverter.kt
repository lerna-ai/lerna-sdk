package ai.lerna.multiplatform.service.converter

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsApi
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItemApi
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.operations.toMutableList

class DLArrayConverter {
	fun convert(array: DoubleArray): D2Array<Double> {
		return mk.ndarray(array).reshape(array.size, 1)
	}

	fun convert(d2Array: D2Array<Double>): DoubleArray {
		return d2Array.toMutableList().toDoubleArray()
	}

	fun convert(globalTrainingWeightsApi: GlobalTrainingWeightsApi): GlobalTrainingWeights {
		val globalTrainingWeights = GlobalTrainingWeights()
		globalTrainingWeights.version = globalTrainingWeightsApi.version
		globalTrainingWeights.trainingWeights = globalTrainingWeightsApi.trainingWeights?.map { item -> convert(item) }
		return globalTrainingWeights
	}

	fun convert(globalTrainingWeights: GlobalTrainingWeights): GlobalTrainingWeightsApi {
		val globalTrainingWeightsApi = GlobalTrainingWeightsApi()
		globalTrainingWeightsApi.version = globalTrainingWeights.version
		globalTrainingWeightsApi.trainingWeights = globalTrainingWeights.trainingWeights?.map { item -> convert(item) }
		return globalTrainingWeightsApi
	}

	fun convert(globalTrainingWeightsItemApi: GlobalTrainingWeightsItemApi): GlobalTrainingWeightsItem {
		val globalTrainingWeightsItem = GlobalTrainingWeightsItem()
		globalTrainingWeightsItem.weights = globalTrainingWeightsItemApi.weights?.entries?.associate { (key, value) -> key to convert(value) }
		globalTrainingWeightsItem.mlId = globalTrainingWeightsItemApi.mlId
		globalTrainingWeightsItem.mlName = globalTrainingWeightsItemApi.mlName
		globalTrainingWeightsItem.accuracy = globalTrainingWeightsItemApi.accuracy
		return globalTrainingWeightsItem
	}

	fun convert(globalTrainingWeightsItem: GlobalTrainingWeightsItem): GlobalTrainingWeightsItemApi {
		val globalTrainingWeightsItemApi = GlobalTrainingWeightsItemApi()
		globalTrainingWeightsItemApi.weights = globalTrainingWeightsItem.weights?.entries?.associate { (key, value) -> key to convert(value) }
		globalTrainingWeightsItemApi.mlId = globalTrainingWeightsItem.mlId
		globalTrainingWeightsItemApi.mlName = globalTrainingWeightsItem.mlName
		globalTrainingWeightsItemApi.accuracy = globalTrainingWeightsItem.accuracy
		return globalTrainingWeightsItemApi
	}
}