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
	//internal var inferHistory: MutableList<String> = ArrayList()
	private var thetaClass = HashMap<String, D2Array<Float>>()

	internal fun setWeights(trainingWeights: GlobalTrainingWeightsItem) {
		thetaClass.clear()
		trainingWeights.weights!!.forEach { (k, v) ->
			thetaClass[k] = v
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
		var value = "0"
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
		//inferHistory.add(value)

		return value
	}

	/*
	 * 1 line per item, multiple items - return the percentages - take class as input
	 */
	internal fun predictLabelScore1LineMulItems(testFeatures: Pair<Array<String>, D2Array<Float>>, thetaName: String?): Map<String, Float>? {
		val name = if(thetaName!=null) {
			if (!thetaClass.containsKey(thetaName)) {
				Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLInfer")
				return null
			} else {
				thetaName
			}
		} else {
			thetaClass.keys.first()
		}

		val outputs = calculateOutput(testFeatures.second, thetaClass[name]!!)

		val result = HashMap<String, Float>()

		for (i in 0 until outputs.shape[0]) {
			result[testFeatures.first[i]] = outputs.asD2Array()[i, 0]
		}

		return result
	}

	/*
	 * Multiple lines per item, 1 item - return the total score - take class as input
	 */
	private fun predictLabelScoreMulLines1Item(testFeatures: D2Array<Float>, thetaName: String?): Float {
		val name = if(thetaName!=null) {
			if (!thetaClass.containsKey(thetaName)) {
				Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLInfer")
				return -1.0f
			} else {
				thetaName
			}
		} else {
			thetaClass.keys.first()
		}

		val outputs = calculateOutput(testFeatures, thetaClass[name]!!)

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
	internal fun predictLabelScoreMulLinesMulItems(testFeatures: Map<String, D2Array<Float>>, thetaName: String?): Map<String, Float>? {
		val name = if(thetaName!=null) {
			if (!thetaClass.containsKey(thetaName)) {
				Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLInfer")
				return null
			} else {
				thetaName
			}
		} else {
			thetaClass.keys.first()
		}
		val result = HashMap<String, Float>()
		testFeatures.forEach { (k, v) ->
			result[k] = predictLabelScoreMulLines1Item(v, name)
		}
		return result
	}

	private fun calculateOutput(X: D2Array<Float>, theta: D2Array<Float>): D2Array<Float> {
		val z = X.dot(theta)
		return sigmoid(z)
	}

//	internal fun clearHistory() {
//		inferHistory.clear()
//	}

	private fun sigmoid(Z: D2Array<Float>): D2Array<Float> {
		// S(Z) = 1 / ( 1 - e ^ (-Z))
		return 1.0f.div(
			Z.times(-1.0f)
				.exp()
				.plus(1.0f)
		)
	}

}