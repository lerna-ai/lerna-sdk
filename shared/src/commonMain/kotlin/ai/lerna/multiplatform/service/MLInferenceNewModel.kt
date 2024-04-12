package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.advancedML.BinaryCELossCalculator
import ai.lerna.multiplatform.service.advancedML.CustomTrainer
import ai.lerna.multiplatform.service.advancedML.SimpleExample
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.TrainingTasks
import com.kotlinnlp.simplednn.core.functionalities.updatemethods.adam.ADAMMethod
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import io.github.aakira.napier.Napier
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.ones
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get



class MLInferenceNewModel {
    //internal var inferHistory: MutableList<String> = ArrayList()
    var thetaClass = mutableMapOf<String, CustomTrainer<DenseNDArray>?>()


    internal fun setWeights(trainingWeights: GlobalTrainingWeightsItem) {
        val epoch = trainingWeights.epochs
        val lr = trainingWeights.lr
        val dimensions = trainingWeights.dimentions
        val method = trainingWeights.method
        thetaClass.clear()
        trainingWeights.weightsMultiKv2!!.forEach { (k, v) ->
            this.thetaClass[k] = CustomTrainer(
                model = method?:"LR",
                updateMethod = ADAMMethod(stepSize = lr?:0.001f),
                lossCalculator = BinaryCELossCalculator(),
                examples = listOf(SimpleExample(null,null,null, if(v.sensors!=null) mk.ndarray(arrayOf(FloatArray(v.sensors!!.first().first.first().shape[1]){0.0f})) else null,null)),
                epochs = epoch?:10,
                batchSize = 1,
                testExamples = listOf(SimpleExample(null,null,null,null,null)),
                totalFeatures = dimensions?:0,
                verbose = false)
            this.thetaClass[k]?.reset()
            this.thetaClass[k]?.setWeights(v)
        }
    }

    /*
     * 1 line and item - choose the most probable outcome out of all classes
     */
    internal fun predictLabelFrom1Line1Item(testFeatures: SimpleExample): String {


        val outputs = mutableMapOf<String, Float>()
        thetaClass.forEach { (k, v) ->
            val predictedLabel = v!!.neuralProcessor.forward(testFeatures)[0]
            outputs[k] = predictedLabel
        }

        //Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
        var max = 0.0f //if more than 1 class, always pick the most probable one even if the probability is very low
        var value = "failure"
        if (thetaClass.size == 1) {
            max = 0.5f //give success only if confidence is more than 50% (in case we have only 1 class, i.e., success/failure)
        }

        outputs.forEach { (k, _) ->
            if (outputs[k]!! > max) {
                max = outputs[k]!!
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
    internal fun predictLabelScore1LineMulItems(testFeatures: Pair<Array<String>, List<SimpleExample>>, thetaName: String?): Map<String, Float>? {
        val name = if(thetaName!=null) {
            if (!thetaClass.containsKey(thetaName)) {
                Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLNewModelInfer")
                return null
            } else {
                thetaName
            }
        } else {
            thetaClass.keys.first()
        }


        //val outputs = mutableMapOf<String, Array<Double>>()

        val predictedLabels = Array(testFeatures.second.size){-1.0f}
        var i=0
        testFeatures.second.forEach{
            predictedLabels[i] = thetaClass[name]!!.neuralProcessor.forward(it)[0]
            i++
        }

        val result = mutableMapOf<String, Float>()

        for (j in predictedLabels.indices) {
            result[testFeatures.first[j]] = predictedLabels[j].toFloat()
        }

        return result
    }

    /*
     * Multiple lines per item, 1 item - return the total score - take class as input
     */
    private fun predictLabelScoreMulLines1Item(testFeatures: List<SimpleExample>, thetaName: String?): Float {
        val name = if(thetaName!=null) {
            if (!thetaClass.containsKey(thetaName)) {
                Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLNewModelInfer")
                return -1.0f
            } else {
                thetaName
            }
        } else {
            thetaClass.keys.first()
        }



        val predictedLabels = Array(testFeatures.size){-1.0f}
        var i=0
        testFeatures.forEach{
            predictedLabels[i] = thetaClass[name]!!.neuralProcessor.forward(it)[0]
            i++
        }


        var result = 0.0f

        for (element in predictedLabels) {
            //Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
            result += element
        }

        return result.toFloat()
    }

    /*
     * Multiple lines per item, multiple items - return the scores - take class as input
     */
    internal fun predictLabelScoreMulLinesMulItems(testFeatures: Map<String, List<SimpleExample>>, thetaName: String?): Map<String, Float>? {
        val name = if(thetaName!=null) {
            if (!thetaClass.containsKey(thetaName)) {
                Napier.e("No class $thetaName exists in ${thetaClass.keys}", null, "LernaMLNewModelInfer")
                return null
            } else {
                thetaName
            }
        } else {
            thetaClass.keys.first()
        }
        val result = mutableMapOf<String, Float>()
        testFeatures.forEach { (k, v) ->
            result[k] = predictLabelScoreMulLines1Item(v, name)
        }
        return result
    }
}