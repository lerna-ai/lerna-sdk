import ai.lerna.multiplatform.service.IMLExecution
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.TrainingTasks
import com.soywiz.korio.file.std.cacheVfs
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.math.exp
import org.jetbrains.kotlinx.multik.api.stat.abs
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import io.github.aakira.napier.Napier
import kotlin.math.ceil

class MLExecution(_task: TrainingTasks) : IMLExecution {
    private lateinit var trainFeatures: D2Array<Float>
    private lateinit var testFeatures: D2Array<Float>
    private lateinit var trainLabels: D2Array<Float>
    private lateinit var testLabels: D2Array<Float>
    var thetaClass = HashMap<String, D2Array<Float>>()
    private val task = _task
    private lateinit var next: List<FloatArray>


    override suspend fun loadData() {
        next = getData()
    }

    override fun prepareData(ml_id: Int) {
        val list = next.map { it[0] }.distinct()
        val samples = ceil(task.trainingTasks!![ml_id].lernaMLParameters!!.dataSplit!!.toFloat() / 100.0 * list.size.toFloat()).toInt()
        val sessions = list.asSequence().shuffled().take(samples).toList()

        val trainData = mutableListOf<FloatArray>()
        val testData = mutableListOf<FloatArray>()

        for (i in next.indices) {
            if (sessions.contains(next[i][0])) {
                trainData.add(next[i])
            }
            else {
                testData.add(next[i])
            }
        }
        val train = mk.ndarray(trainData.toTypedArray())
        val test = mk.ndarray(testData.toTypedArray())

        var range=IntRange(1, trainData[0].size-1)

        trainFeatures = train.slice<Float,D2,D2>(range, 1).transpose()
        trainFeatures = mk.ones<Float>(trainData.size).cat(trainFeatures.flatten()).reshape(range.count(), trainData.size)
        trainFeatures = trainFeatures.transpose()

        testFeatures = test.slice<Float,D2,D2>(range, 1).transpose()
        testFeatures = mk.ones<Float>(testData.size).cat(testFeatures.flatten()).reshape(range.count(), testData.size)
        testFeatures = testFeatures.transpose()

        range = IntRange(trainData[0].size-1, trainData[0].size)

        trainLabels = train.slice(range, 1)
        testLabels = test.slice(range, 1)


        Napier.d("LernaML - Data size: " + trainData.size + " and " + testData.size)
    }

    override fun localML(ml_id: Int): Long {
        if (thetaClass.isEmpty()) {
            Napier.e("LernaML - Cannot train without initializing the weights first")
            //thetaClass["1.0"] = mk.rand<Float>(trainFeatures[0].size, 1)
            //thetaClass["2.0"] = mk.rand<Float>(trainFeatures[0].size, 1)
            //thetaClass["4.0"] = mk.rand<Float>(trainFeatures[0].size, 1)
            return -1L
        }

        val a = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.normalization
        val epoch = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.iterations
        val lr = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.learningRate

        thetaClass.forEach { (key, _) ->
            thetaClass[key] = training(
                thetaClass[key]!!,
                lr!!.toFloat(),
                trainFeatures,
                filterClassLabels(trainLabels, key.toFloat()),
                epoch,
                0.0001f,
                a!!.toFloat()
            )
        }

        return (trainLabels.size + testLabels.size).toLong()
    }

    override fun addNoise(share: Float, scaling: Int, prediction: String): D2Array<Float>? {
        return thetaClass[mapping(prediction)]?.times(scaling.toFloat())?.plus(share)
    }


    override fun setWeights(trainingWeights: GlobalTrainingWeightsItem) {
        thetaClass.clear()
        trainingWeights.weights!!.forEach { (k, v) ->
            thetaClass[mapping(k)] = v
        }
    }

    override fun computeAccuracy(): Float {
        val predictedLabels = predictLabels(testFeatures)
        val correctSamples: Float = countCorrectSamples(testLabels, predictedLabels)
        val accuracy = correctSamples / testLabels.size.toFloat()
        Napier.d("LernaML - Correct samples: $correctSamples")
        Napier.d("LernaML - Accuracy: " + accuracy * 100 + "%")
        return accuracy
    }

    private fun predictLabels(testFeatures: D2Array<Float>): FloatArray {
        val outputs = HashMap<String, D2Array<Float>>()
        thetaClass.forEach { (k, v) ->
            outputs[k] = calculateOutput(testFeatures, v)
        }
        val result = Array(outputs.values.toList()[0].shape[0]){0.0f}

        for (i in 0 until outputs.values.toList()[0].shape[0]) {
            //Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
            var max = 0.0f //if more than 1 class, always pick the most probable one even if the probability is very low
            var value = "0.0"
            if(thetaClass.size==1)
                max = 0.5f //give success only if confidence is more than 50% (in case we have only 1 class, i.e., success/failure)
            outputs.forEach { (k, _) ->
                if (outputs[k]!!.asD2Array()[i, 0] > max) {
                    max = outputs[k]!!.asD2Array()[i, 0]
                    value = k
                }
            }
            //Napier.i("chosen $i, $value")
            result[i] = value.toFloat()
        }

        return result.toFloatArray()
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

    private fun calculateOutput(X: D2Array<Float>, theta: D2Array<Float>): D2Array<Float> {
        val z = X.dot(theta)
        return sigmoid(z)
    }

    private fun gradientFunction(
        theta: D2Array<Float>,
        X: D2Array<Float>,
        y: D2Array<Float>,
        a: Float,
        lr: Float
    ): D2Array<Float> {
        //number of samples
        val m = X.shape[0]

        val h = calculateOutput(X, theta)
        // difference between predicted and actual class
        val diff = h.minus(y)
        return X.transpose().dot(diff).times(lr).plus(a * theta.sum().toFloat()).times(1.0f / m)
    }

    private fun hasConverged(oldTheta: D2Array<Float>, newTheta: D2Array<Float>, epsilon: Float): Boolean {
        val diffSum = abs(oldTheta.minus(newTheta)).sum().toFloat()
        return (diffSum / oldTheta.shape[0] < epsilon)
    }

    private fun training(
        theta: D2Array<Float>,
        lr: Float,
        X: D2Array<Float>,
        y: D2Array<Float>,
        maxIterations: Int,
        epsilon: Float,
        a: Float
    ): D2Array<Float> {
        //set random seed
        var oldTheta = theta.copy()
        var newTheta = theta.copy()

        //optimalTheta = theta.dup()
        for (i in 1..maxIterations) {
            var gradients: D2Array<Float>? = gradientFunction(oldTheta, X, y, a, lr)

            //calculate new theta with gradients and learning rate alpha
            //gradients = gradients.mul(alpha)
            newTheta = oldTheta.minus(gradients!!)

            if (hasConverged(oldTheta, newTheta, epsilon)) {
                break
            }
            oldTheta = newTheta
            //gradients!!.cleanup()
            gradients = null
        }
        return newTheta
    }

    private suspend fun getData(): List<FloatArray>{
        val mlData = cacheVfs["mldata.csv"].readLines().toList()
            .filter { it.isNotEmpty() }
            .map { line -> line.split(",")
                .filter { !it.contains("_") }
                .map { it.toFloat() }
                .toFloatArray() }
        cacheVfs["mldata.csv"].delete()
        return mlData
    }

    private fun filterClassLabels(labels: D2Array<Float>, label: Float): D2Array<Float> {
        //returns an array with zeros where labels[i]!=label and ones where labels[i]==label
        val classn: D2Array<Float> = labels.copy()
        return classn.map { if ( it != label) 0.0f else it}
    }

    private fun countCorrectSamples(labels: D2Array<Float>, predictedLabels: FloatArray): Float {
        var correctSamples = 0
        for (i in 0 until labels.size) {
            if (labels[i][0] == predictedLabels[i]) {
                correctSamples++
            }
        }
        return correctSamples.toFloat()
    }

    fun mapping(prediction: String): String {
        return when (prediction) {
            "audio" -> "1.0"
            "game" -> "2.0"
            "image" -> "3.0"
            "maps" -> "4.0"
            "news" -> "5.0"
            "productivity" -> "6.0"
            "social" -> "7.0"
            "video" -> "8.0"
            "1.0", "1" -> "audio"
            "2.0", "2" -> "game"
            "3.0", "3" -> "image"
            "4.0", "4" -> "maps"
            "5.0", "5" -> "news"
            "6.0", "6" -> "productivity"
            "7.0", "7" -> "social"
            "8.0", "8" -> "video"
            "0.0", "0" -> "undefined/none"
            else -> "0.0"
        }
    }
}
