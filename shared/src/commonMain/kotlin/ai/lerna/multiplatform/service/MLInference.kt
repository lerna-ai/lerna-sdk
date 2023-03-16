package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import io.github.aakira.napier.Napier
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.math.exp
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ones
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.times


class MLInference() {
	internal var inferHistory: MutableList<String> = ArrayList()
	private var thetaClass = HashMap<String, D2Array<Float>>()

	internal fun setWeights(trainingWeights: GlobalTrainingWeightsItem) {
		thetaClass.clear()
		trainingWeights.weights!!.forEach { (k, v) ->
			thetaClass[mapping(k)] = v
		}
	}

	/*
	 * 1 line and item - choose the most probable outcome out of all classes
	 */
	internal fun predictLabelFrom1Line1Item(testFeatures: D2Array<Float>): String {
		val features = mk.ones<Float>(testFeatures.shape[0]).cat(testFeatures.flatten())
			.reshape(testFeatures.shape[1] + 1, testFeatures.shape[0])
			.transpose()

		val outputs = HashMap<String, D2Array<Float>>()
		thetaClass.forEach { (k, v) ->
			outputs[k] = calculateOutput(features, v)
		}

		//Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
		var max = 0.0f //if more than 1 class, always pick the most probable one even if the probability is very low
		var value = "0.0"
		if (thetaClass.size == 1) {
			max = 0.5f //give success only if confidence is more than 50% (in case we have only 1 class, i.e., success/failure)
		}

		outputs.forEach { (k, _) ->
			if (outputs[k]!!.asD2Array()[0, 0] > max) {
				max = outputs[k]!!.asD2Array()[0, 0]
				value = k
			}
		}
		//Napier.i("chosen $i, $value")
		inferHistory.add(value)

		return value
	}

	/*
	 * 1 line per item, multiple items - return the percentages - take class as input
	 */
	internal fun predictLabelScore1LineMulItems(testFeatures: D2Array<Float>, thetaName: String): FloatArray? {
		if (!thetaClass.containsKey(thetaName)) {
			Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLInfer")
			return null
		}

		val outputs = calculateOutput(testFeatures, thetaClass[thetaName]!!)

		val result = Array(outputs.shape[0]) { 0.0f }

		for (i in 0 until outputs.shape[0]) {
			result[i] = outputs.asD2Array()[i, 0]
		}

		return result.toFloatArray()
	}

	/*
	 * Multiple lines per item, 1 item - return the total score - take class as input
	 */
	private fun predictLabelScoreMulLines1Item(testFeatures: D2Array<Float>, thetaName: String): Float {
		if (!thetaClass.containsKey(thetaName)) {
			Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLInfer")
			return -1.0f
		}

		val outputs = calculateOutput(testFeatures, thetaClass[thetaName]!!)

		var result = 0.0f

		for (i in 0 until outputs.shape[0]) {
			//Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
			result += outputs.asD2Array()[i, 0]
		}

		return result
	}

	/*
	 * Multiple lines per item, multiple items - return the scores - take class as input
	 */
	internal fun predictLabelScoreMulLinesMulItems(testFeatures: Array<D2Array<Float>>, thetaName: String): FloatArray? {
		if (!thetaClass.containsKey(thetaName)) {
			Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLInfer")
			return null
		}
		val result = Array(testFeatures.size) { 0.0f }
		for (i in testFeatures.indices) {
			result[i] = predictLabelScoreMulLines1Item(testFeatures[i], thetaName)
		}
		return result.toFloatArray()
	}

	private fun calculateOutput(X: D2Array<Float>, theta: D2Array<Float>): D2Array<Float> {
		val z = X.dot(theta)
		return sigmoid(z)
	}

	internal fun clearHistory() {
		inferHistory.clear()
	}

	private fun sigmoid(Z: D2Array<Float>): D2Array<Float> {
		// S(Z) = 1 / ( 1 - e ^ (-Z))
		return 1.0f.div(
			Z.times(-1.0f)
				.exp()
				.plus(1.0f)
		)
	}

	private fun mapping(prediction: String): String {
		return when (prediction) {
			"audio" -> "1.0"
			"game" -> "2.0"
			"image" -> "3.0"
			"maps" -> "4.0"
			"news" -> "5.0"
			"productivity" -> "6.0"
			"social" -> "7.0"
			"video" -> "8.0"
			else -> "0.0"
		}

	}
}