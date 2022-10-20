package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem

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
    var inferHistory: MutableList<Int> = ArrayList()
    private var thetaClass = HashMap<String, D2Array<Float>>()

    fun setWeights(trainingWeights: GlobalTrainingWeightsItem) {
        thetaClass.clear()
        trainingWeights.weights!!.forEach { (k, v) ->
            thetaClass[mapping(k)] = v
        }
    }


    fun predictLabels(testFeatures: D2Array<Float>): String {

        var features = mk.ones<Float>(testFeatures.shape[0]).cat(testFeatures.flatten()).reshape(testFeatures.shape[1]+1, testFeatures.shape[0])
        features = features.transpose()

        val outputs = HashMap<String, D2Array<Float>>()
        thetaClass.forEach { (k, v) ->
            outputs[k] = calculateOutput(features, v)
        }



            //Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
            var max = 0.0f
            var value = "0.0"
            outputs.forEach { (k, _) ->
                if (outputs[k]!!.asD2Array()[0, 0] > max) {
                    max = outputs[k]!!.asD2Array()[0, 0]
                    value = k
                }
            }
            //Napier.i("chosen $i, $value")
        inferHistory.add(value.toFloat().toInt())


        return value
    }

    private fun calculateOutput(X: D2Array<Float>, theta: D2Array<Float>): D2Array<Float> {
        val z = X.dot(theta)
        return sigmoid(z)
    }

    fun clearHistory() {
        inferHistory.clear()
    }

    private fun sigmoid(Z: D2Array<Float>): D2Array<Float> {
        //Z = (-Z)
        var z = Z.times(-1.0f)
        //Z = e^(Z); do not create new array
        z = z.exp()
        //1 + Z
        z = z.plus(1.0f)
        //1.0 / Z
        z = 1.0f.div(z)
        return z
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