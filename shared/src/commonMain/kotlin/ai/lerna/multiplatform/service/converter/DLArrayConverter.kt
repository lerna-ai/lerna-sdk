package ai.lerna.multiplatform.service.converter

import ai.lerna.multiplatform.service.dto.AdvancedMLItem
import ai.lerna.multiplatform.service.dto.AdvancedMLItemApi
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsApi
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItemApi
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray
import org.jetbrains.kotlinx.multik.ndarray.operations.toMutableList

class DLArrayConverter {

	fun convert(weights: AdvancedMLItemApi): AdvancedMLItem {
		val output = AdvancedMLItem()
		output.embedding = weights.embedding?.entries?.associate { (key, value) -> key to convert2d(value) }
		output.attention = weights.attention?.map {
			return@map Pair(it.first.map{it2 -> convert2d(it2)}.toList(), it.second.map{it3 -> convert2d(it3)}.toList())
		}?.toList()
		output.sensors = weights.sensors?.map {
			return@map Pair(it.first.map{it2 -> convert2d(it2)}.toList(), it.second.map{it3 -> convert2d(it3)}.toList())
		}?.toList()
		output.lastlayer = weights.lastlayer?.map {
			return@map Pair(it.first.map{it2 -> convert2d(it2)}.toList(), it.second.map{it3 -> convert2d(it3)}.toList())
		}?.toList()
		return output
	}

	fun convert(weights: AdvancedMLItem): AdvancedMLItemApi {
		val output = AdvancedMLItemApi()
		output.embedding = weights.embedding?.entries?.associate { (key, value) -> key to convert2d(value) }
		output.attention = weights.attention?.map {
			return@map Pair(it.first.map{it2 -> convert2d(it2)}.toList(), it.second.map{it3 -> convert2d(it3)}.toList())
		}?.toList()
		output.sensors = weights.sensors?.map {
			return@map Pair(it.first.map{it2 -> convert2d(it2)}.toList(), it.second.map{it3 -> convert2d(it3)}.toList())
		}?.toList()
		output.lastlayer = weights.lastlayer?.map {
			return@map Pair(it.first.map{it2 -> convert2d(it2)}.toList(), it.second.map{it3 -> convert2d(it3)}.toList())
		}?.toList()
		return output
	}

	fun convert(array: FloatArray): D2Array<Float> {
		return mk.ndarray(array).reshape(array.size, 1)
	}

	fun convert2d(d2Array: D2Array<Float>): Array<FloatArray> {
		val output = Array(d2Array.shape[0]) { floatArrayOf()}

		for(i in 0..<d2Array.shape[0]) {
			output[i] = d2Array[i].toFloatArray()
		}
		return output
	}

	fun convert2d(array: Array<FloatArray>): D2Array<Float> {
		return mk.ndarray(array)//.reshape(array.size, array[0].size)
	}

	fun convert(d2Array: D2Array<Float>): FloatArray {
		return d2Array.toMutableList().toFloatArray()
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
		globalTrainingWeightsItem.weightsMultiKv2 = globalTrainingWeightsItemApi.weightsMultiKv2?.entries?.associate { (key, value) -> key to convert(value) }
		globalTrainingWeightsItem.dimentions = globalTrainingWeightsItemApi.dimentions
		globalTrainingWeightsItem.epochs = globalTrainingWeightsItemApi.epochs
		globalTrainingWeightsItem.lr = globalTrainingWeightsItemApi.lr
		globalTrainingWeightsItem.method = globalTrainingWeightsItemApi.method
		return globalTrainingWeightsItem
	}

	fun convert(globalTrainingWeightsItem: GlobalTrainingWeightsItem): GlobalTrainingWeightsItemApi {
		val globalTrainingWeightsItemApi = GlobalTrainingWeightsItemApi()
		globalTrainingWeightsItemApi.weights = globalTrainingWeightsItem.weights?.entries?.associate { (key, value) -> key to convert(value) }
		globalTrainingWeightsItemApi.mlId = globalTrainingWeightsItem.mlId
		globalTrainingWeightsItemApi.mlName = globalTrainingWeightsItem.mlName
		globalTrainingWeightsItemApi.accuracy = globalTrainingWeightsItem.accuracy
		globalTrainingWeightsItemApi.weightsMultiKv2 = globalTrainingWeightsItem.weightsMultiKv2?.entries?.associate { (key, value) -> key to convert(value) }
		globalTrainingWeightsItemApi.dimentions = globalTrainingWeightsItem.dimentions
		globalTrainingWeightsItemApi.epochs = globalTrainingWeightsItem.epochs
		globalTrainingWeightsItemApi.lr = globalTrainingWeightsItem.lr
		globalTrainingWeightsItemApi.method = globalTrainingWeightsItem.method
		return globalTrainingWeightsItemApi
	}
}