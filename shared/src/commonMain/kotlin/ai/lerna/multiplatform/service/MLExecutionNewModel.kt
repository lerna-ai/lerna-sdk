package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.advancedML.CustomTrainer
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import com.kotlinnlp.simplednn.core.functionalities.updatemethods.adam.ADAMMethod
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import korlibs.io.file.std.applicationDataVfs
import org.jetbrains.kotlinx.multik.ndarray.data.*
import ai.lerna.multiplatform.service.advancedML.BinaryCELossCalculator
import ai.lerna.multiplatform.service.advancedML.SimpleExample
import ai.lerna.multiplatform.service.dto.AdvancedMLItem
import ai.lerna.multiplatform.service.dto.TrainingTasks
import io.github.aakira.napier.Napier
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import kotlin.math.ceil


class MLExecutionNewModel(_task: TrainingTasks): IMLExecution {
    private lateinit var trainFeatures: List<SimpleExample>
    private lateinit var testFeatures: List<SimpleExample>
    private lateinit var trainLabels: Array<String>
    private lateinit var testLabels: Array<String>
    var thetaClass = mutableMapOf<String, CustomTrainer<DenseNDArray>?>()
    private lateinit var nextFeatures: List<FloatArray>
    private lateinit var nextLabels: List<Pair<Float, String>>
    private val task = _task



    override suspend fun loadData(filename: String, deleteAfter: Boolean){
        val mlData = applicationDataVfs[filename].readLines().toList()
            .filter { it.isNotEmpty() }

        nextFeatures=mlData
            .map { line -> line.split(",").dropLast(1)
                .filter { !it.contains("_") }
                .map { it.toFloat() }
                .toFloatArray() }

        nextLabels=mlData
            .map { line -> Pair(line.split(",").first().toFloat(), line.split(",").last())
            }
        if(deleteAfter)
            applicationDataVfs[filename].delete()
    }

    override fun prepareData(ml_id: Int, featureSize: Int): Boolean {
        val list = nextFeatures.map { it[0] }.distinct()
        val samples = ceil(task.trainingTasks!![ml_id].lernaMLParameters!!.dataSplit!!.toFloat() / 100.0 * list.size.toFloat()).toInt()
        val sessions = list.asSequence().shuffled().take(samples).toList()

        val trainDataFeatures = mutableListOf<FloatArray>()
        val testDataFeatures = mutableListOf<FloatArray>()
        val trainDataLabels = mutableListOf<String>()
        val testDataLabels = mutableListOf<String>()

        for (i in nextFeatures.indices) {
            // Ignore line if not equal with feature size
            if (nextFeatures[i].size != featureSize) {
                continue
            }
            if (sessions.contains(nextFeatures[i][0])) {
                trainDataFeatures.add(nextFeatures[i])
                trainDataLabels.add(nextLabels[i].second)
            }
            else {
                testDataFeatures.add(nextFeatures[i])
                testDataLabels.add(nextLabels[i].second)
            }
        }

        if (trainDataFeatures.isEmpty() || testDataFeatures.isEmpty())
            return false
        trainLabels = trainDataLabels.toTypedArray()
        testLabels = testDataLabels.toTypedArray()

        val trainExamples: List<SimpleExample> = trainLabels.indices.map {
            SimpleExample(
                //train_categIndexes[it],
                null,
                //train_numIndexes[it], train_numValues[it],
                null,null,
                //listOf(train_multiHot_indexes[it]), listOf(train_multiHot_values[it]),
                null, null,
                trainDataFeatures[it],
                null
                //trainLabels[it]
            )
        }

        trainFeatures = trainExamples


        val testExamples: List<SimpleExample> = testLabels.indices.map {
            SimpleExample(
                //test_categIndexes[it],
                null,
                //test_numIndexes[it], test_numValues[it],
                null,null,
                //listOf(test_multiHot_indexes[it]), listOf(test_multiHot_values[it]),
                null, null,
                testDataFeatures[it],
                null
                //testLabels[it]
            )
        }

        testFeatures = testExamples

        Napier.d("LernaML - Data size: " + trainFeatures.size + " and " + testFeatures.size)
        return true
    }


    override fun localML(ml_id: Int): Long {

        val epoch = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.iterations
        val lr = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.learningRate
        val dimensions = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.dimensions
        val method = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.method

        thetaClass.forEach { (key, _) ->
            var numLabels = filterClassLabels(trainLabels, key)
            trainFeatures.forEachIndexed{ i,it -> it.addLabels(numLabels[i])}
            numLabels = filterClassLabels(testLabels, key)
            testFeatures.forEachIndexed{ i,it -> it.addLabels(numLabels[i])}
            thetaClass[key] = CustomTrainer(
                model = method?:"LR",
                updateMethod = ADAMMethod(stepSize = lr ?: 0.001f),
                lossCalculator = BinaryCELossCalculator(),
                examples = trainFeatures,
                epochs = epoch,
                batchSize = 1,
                testExamples = testFeatures,
                totalFeatures = dimensions,
                verbose = false)
            println("Start model $key training...")
            thetaClass[key]?.train()
        }

        val ckptWeights = mutableMapOf<String, AdvancedMLItem?>()
        thetaClass.forEach { (key, _) ->
            ckptWeights[key]= thetaClass[key]?.getWeights()
        }

        println("End model training")
        this.computeAccuracy()
        thetaClass.forEach { (key, _) ->
            thetaClass[key]?.reset()
        }
        println("model reset to default, computing test accuracy with random weights...")
        this.computeAccuracy()

        thetaClass.forEach { (key, _) ->
            thetaClass[key]?.setWeights(ckptWeights[key]!!)
        }

        println("AUC after loading weights from checkpoint......")
        this.computeAccuracy()

        return 0
    }

    private fun filterClassLabels(labels: Array<String>, label: String): List<Float> {
        //returns an array with zeros where labels[i]!=label and ones where labels[i]==label
        val classn: List<Float> = labels.map { if ( it != label) 0.0f else 1.0f}
        return classn
    }

    override fun addNoise(share: Float, scaling: Int, prediction: String): AdvancedMLItem {
        val ckptWeights = this.thetaClass[prediction]?.getWeights()
        val embedding = ckptWeights?.embedding?.mapValues { it.value.times(scaling.toFloat()).plus(share) }
        val sensors = ckptWeights?.sensors?.map {
            return@map Pair(it.first.map{it2 -> it2.times(scaling.toFloat()).plus(share)}.toList(), it.second.map{it3 -> it3.times(scaling.toFloat()).plus(share)}.toList())
        }?.toList()
        val attention = ckptWeights?.attention?.map {
            return@map Pair(it.first.map{it2 -> it2.times(scaling.toFloat()).plus(share)}.toList(), it.second.map{it3 -> it3.times(scaling.toFloat()).plus(share)}.toList())
        }?.toList()
        val lastlayer = ckptWeights?.lastlayer?.map {
            return@map Pair(it.first.map{it2 -> it2.times(scaling.toFloat()).plus(share)}.toList(), it.second.map{it3 -> it3.times(scaling.toFloat()).plus(share)}.toList())
        }?.toList()
        val newWeights = AdvancedMLItem()
        newWeights.embedding = embedding
        newWeights.sensors = sensors
        newWeights.attention = attention
        newWeights.lastlayer = lastlayer
        return newWeights
    }


    override fun setWeights(trainingWeights: GlobalTrainingWeightsItem) {

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





    override fun computeAccuracy(): Float {
        val predictedLabels = predictLabels(testFeatures)
        //println("Original: "+testLabels.toList())
        //println("Predicted: "+predictedLabels.toList())
        val correctSamples: Float = countCorrectSamples(testLabels, predictedLabels)
        val accuracy = correctSamples / testLabels.size.toFloat()
        Napier.d("LernaML - Correct samples: $correctSamples")
        Napier.d("LernaML - Accuracy: " + accuracy * 100 + "%")
        return accuracy.toFloat()
    }

    private fun predictLabels(examples: List<SimpleExample>): Array<String> {
        val outputs = mutableMapOf<String, Array<Float>>()
        thetaClass.forEach { (k, v) ->
            val predictedLabels = Array(examples.size){-1.0f}
            var i=0
            examples.forEach{
                predictedLabels[i] = v!!.neuralProcessor.forward(it)[0]
                i++
            }
            outputs[k] = predictedLabels
        }
        val result = Array(outputs.values.toList()[0].size){"failure"}

        for (i in 0 until outputs.values.toList()[0].size) {
            //Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
            var max = 0.0f //if more than 1 class, always pick the most probable one even if the probability is very low
            var value = "failure"
            if(thetaClass.size==1)
                max = 0.5f //give success only if confidence is more than 50% (in case we have only 1 class, i.e., success/failure)
            outputs.forEach { (k, _) ->
                if (outputs[k]!![i] > max) {
                    max = outputs[k]!![i]
                    value = k
                }
            }
            //Napier.i("chosen $i, $value")
            result[i] = value
        }

        return result
    }

    private fun countCorrectSamples(labels: Array<String>, predictedLabels: Array<String>): Float {
        var correctSamples = 0
        for (i in labels.indices) {
            if (labels[i] == predictedLabels[i]) {
                correctSamples++
            }
        }
        return correctSamples.toFloat()
    }

}