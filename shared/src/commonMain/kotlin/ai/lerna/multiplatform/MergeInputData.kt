package ai.lerna.multiplatform

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class MergeInputData(modelData: ModelData, length: Int) {
	private val modelData = modelData
	private val length = length
	private var inputDataMap = mutableMapOf<String, FloatArray>()

	internal fun putItem(name: String, value: FloatArray) {
		if (value.size != length) {
			throw IllegalArgumentException("Incorrect size of value array")
		}
		inputDataMap[name] = value
	}

	internal fun putItems(inferenceMap: Map<String, FloatArray>) {
		inferenceMap.values.forEach {
			if (it.size != length) {
				throw IllegalArgumentException("Incorrect size of value array")
			}
		}
		inputDataMap = inferenceMap.toMutableMap()
	}

	internal fun clear() {
		inputDataMap.clear()
	}

	internal fun getMergedInputData(): Pair<Array<String>, D2Array<Float>> {
		val sensorData = modelData.toArray().toFloatArray()
		val names = mutableListOf<String>()
		val inferenceInputData = mutableListOf<FloatArray>()
		for (item in inputDataMap) {
			names.add(item.key)
			inferenceInputData.add(sensorData + item.value)
		}
		return Pair(names.toTypedArray(), mk.ndarray(inferenceInputData.toTypedArray()))
	}

	internal fun getMergedInputDataHistory(): Map<String, D2Array<Float>> {
		val sensorDataHistory = modelData.historyToArray()
		val inferenceInputDataHistory = mutableMapOf<String, D2Array<Float>>()
		for (item in inputDataMap) {
			val inputDataItem = mutableListOf<FloatArray>()
			sensorDataHistory.forEach { sensor ->
				inputDataItem.add(sensor + item.value)
			}
			inferenceInputDataHistory[item.key] = mk.ndarray(inputDataItem.toTypedArray())
		}
		return inferenceInputDataHistory
	}

	internal fun historyToCsv(sessionID: Int, itemID: String, successValue: String): String {
		val sensorDataHistory = modelData.historyToArray()
		val item = inputDataMap[itemID]!!
		val inputDataItem = mutableListOf<FloatArray>()
		sensorDataHistory.forEach { sensor ->
			inputDataItem.add(sensor + item)
		}
		return inputDataItem.joinToString(
			prefix = "$sessionID,",
			separator = ",$successValue\n$sessionID,",
			postfix = ",$successValue\n") { it.toString() }
	}

	internal fun lastLineToCsv(sessionID: Int, itemID: String, successValue: String): String {
		val sensorData = modelData.toArray().toFloatArray()
		val item = inputDataMap[itemID]!!
		val inputDataItem = mutableListOf<FloatArray>()
		inputDataItem.add(sensorData + item)
		return inputDataItem.joinToString(
			prefix = "$sessionID,",
			separator = ",$successValue\n$sessionID,",
			postfix = ",$successValue\n") { it.toString() }
	}
}