package ai.lerna.multiplatform

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class MergeInputData(private val modelData: ModelData, private val length: Int) {

	internal fun getMergedInputData(inputDataMap: Map<String, FloatArray>): Pair<Array<String>, D2Array<Float>> {
		val sensorData = modelData.toArray().toFloatArray()
		val names = mutableListOf<String>()
		val inferenceInputData = mutableListOf<FloatArray>()

		for (item in inputDataMap) {
			if (item.value.size != length) {
				throw IllegalArgumentException("Incorrect size of value array")
			}
			names.add(item.key)
			inferenceInputData.add(sensorData + item.value)
		}
		return Pair(names.toTypedArray(), mk.ndarray(inferenceInputData.toTypedArray()))
	}

	internal fun getMergedInputDataHistory(inputDataMap: Map<String, FloatArray>): Map<String, D2Array<Float>> {
		val sensorDataHistory = modelData.historyToArray()
		val inferenceInputDataHistory = mutableMapOf<String, D2Array<Float>>()
		for (item in inputDataMap) {
			if (item.value.size != length) {
				throw IllegalArgumentException("Incorrect size of value array")
			}
			val inputDataItem = mutableListOf<FloatArray>()
			sensorDataHistory.forEach { sensor ->
				inputDataItem.add(sensor + item.value)
			}
			if (inputDataItem.isNotEmpty()) {
				inferenceInputDataHistory[item.key] = mk.ndarray(inputDataItem.toTypedArray())
			}
		}
		return inferenceInputDataHistory
	}

	internal fun historyToCsv(item: FloatArray, sessionID: Int, successValue: String): String {
		val sensorDataHistory = modelData.historyToArray()

		if (item.size != length) {
			throw IllegalArgumentException("Incorrect size of value array")
		}
		val inputDataItem = mutableListOf<FloatArray>()
		sensorDataHistory.forEach { sensor ->
			inputDataItem.add(sensor + item)
		}
		return inputDataItem.joinToString(
			prefix = "$sessionID,",
			separator = ",$successValue\n$sessionID,",
			postfix = ",$successValue\n") { it.toString() }
	}

	internal fun historyToD2Array(): D2Array<Float> {
		val sensorDataHistory = modelData.historyToArray()
		return mk.ndarray(sensorDataHistory)
	}
	internal fun lastLineD2Array(): D2Array<Float> {
		val sensorData = modelData.toArray().toFloatArray()
		return mk.ndarray(listOf(sensorData).toTypedArray())
	}


	internal fun lastLineToCsv(inputDataMap: Map<String, FloatArray>, sessionID: Int, itemID: String, successValue: String): String {
		val sensorData = modelData.toArray().toFloatArray()
		val item = inputDataMap[itemID]!!
		if (item.size != length) {
			throw IllegalArgumentException("Incorrect size of value array")
		}
		val inputDataItem = mutableListOf<FloatArray>()
		inputDataItem.add(sensorData + item)
		return inputDataItem.joinToString(
			prefix = "$sessionID,",
			separator = ",$successValue\n$sessionID,",
			postfix = ",$successValue\n") { it.toString() }
	}
}