import ai.lerna.multiplatform.config.ReadWriteFile
import ai.lerna.multiplatform.service.IMLExecution
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.TrainingTasks
import io.github.aakira.napier.DebugAntilog
import org.jetbrains.kotlinx.multik.api.*
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.math.exp
import org.jetbrains.kotlinx.multik.api.stat.abs
import org.jetbrains.kotlinx.multik.ndarray.data.*
import org.jetbrains.kotlinx.multik.ndarray.operations.*
import io.github.aakira.napier.Napier
import kotlin.math.ceil

class MLExecution(_task: TrainingTasks) : IMLExecution {
    private lateinit var trainFeatures: D2Array<Double>
    private lateinit var testFeatures: D2Array<Double>
    private lateinit var trainLabels: D2Array<Double>
    private lateinit var testLabels: D2Array<Double>
    var thetaClass = HashMap<String, D2Array<Double>>()
    private val task = _task
    private lateinit var next: List<DoubleArray>



    override fun loadData() {
        Napier.base(DebugAntilog())
        next = getData()
    }

    override fun prepareData(ml_id: Int) {
        val samples = ceil(task.trainingTasks!![ml_id].lernaMLParameters!!.dataSplit!!.toDouble() / 100.0 * next.size.toDouble()).toInt()
        val list = mutableListOf<Double>()
        for(row in next){
            if (row[0] !in list) { list.add(row[0]) }
        }

        val sessions = list.asSequence().shuffled().take(samples).toList()

        val trainData = mutableListOf<DoubleArray>()
        val testData = mutableListOf<DoubleArray>()

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

        trainFeatures = train.slice<Double,D2,D2>(range, 1).transpose()
        trainFeatures = mk.ones<Double>(trainData.size).cat(trainFeatures.flatten()).reshape(range.count(), trainData.size)
        trainFeatures = trainFeatures.transpose()

        testFeatures = test.slice<Double,D2,D2>(range, 1).transpose()
        testFeatures = mk.ones<Double>(testData.size).cat(testFeatures.flatten()).reshape(range.count(), testData.size)
        testFeatures = testFeatures.transpose()

        range = IntRange(trainData[0].size-1, trainData[0].size)

        trainLabels = train.slice(range, 1)
        testLabels = test.slice(range, 1)


        Napier.d("LernaML - Data size: " + trainData.size + " and " + testData.size)
    }

    override fun localML(ml_id: Int): Long {
        if (thetaClass.isEmpty()) {
            Napier.e("LernaML - Cannot train without initializing the weights first")
            //thetaClass["1.0"] = mk.rand<Double>(trainFeatures[0].size, 1)
            //thetaClass["2.0"] = mk.rand<Double>(trainFeatures[0].size, 1)
            //thetaClass["4.0"] = mk.rand<Double>(trainFeatures[0].size, 1)
            return -1L
        }

        val a = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.normalization
        val epoch = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.iterations
        val lr = task.trainingTasks?.get(ml_id)?.lernaMLParameters!!.learningRate

        thetaClass.forEach { (key, _) ->
            thetaClass[key] = training(
                thetaClass[key]!!,
                lr!!.toDouble(),
                trainFeatures,
                filterClassLabels(trainLabels, key.toDouble()),
                epoch,
                0.0001,
                a!!.toDouble()
            )
        }

        return (trainLabels.size + testLabels.size).toLong()
    }

    override fun addNoise(share: Double, scaling: Int, prediction: String): D2Array<Double>? {
        return thetaClass[mapping(prediction)]?.times(scaling.toDouble())?.plus(share)
    }


    override fun setWeights(trainingWeights: GlobalTrainingWeightsItem) {
        thetaClass.clear()
        trainingWeights.weights!!.forEach { (k, v) ->
            thetaClass[mapping(k)] = v
        }
    }

    override fun computeAccuracy(): Double {
        val predictedLabels = predictLabels(testFeatures)
        val correctSamples: Double = countCorrectSamples(testLabels, predictedLabels)
        val accuracy = correctSamples / testLabels.size.toDouble()
        Napier.d("LernaML - Correct samples: $correctSamples")
        Napier.d("LernaML - Accuracy: " + accuracy * 100 + "%")
        return accuracy
    }

    private fun predictLabels(testFeatures: D2Array<Double>): DoubleArray {
        val outputs = HashMap<String, D2Array<Double>>()
        thetaClass.forEach { (k, v) ->
            outputs[k] = calculateOutput(testFeatures, v)
        }
        val result = Array(outputs.values.toList()[0].shape[0]){0.0}

        for (i in 0 until outputs.values.toList()[0].shape[0]) {
            //Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
            var max = 0.0
            var value = "0.0"
            outputs.forEach { (k, _) ->
                if (outputs[k]!!.asD2Array()[i, 0] > max) {
                    max = outputs[k]!!.asD2Array()[i, 0]
                    value = k
                }
            }
            //Napier.i("chosen $i, $value")
            result[i] = value.toDouble()
        }

        return result.toDoubleArray()
    }

    private fun sigmoid(Z: D2Array<Double>): D2Array<Double> {
        //Z = (-Z)
        var z = Z.times(-1.0)
        //Z = e^(Z); do not create new array
        z = z.exp()
        //1 + Z
        z = z.plus(1.0)
        //1.0 / Z
        z = 1.0.div(z)
        return z
    }

    private fun calculateOutput(X: D2Array<Double>, theta: D2Array<Double>): D2Array<Double> {
        val z = X.dot(theta)
        return sigmoid(z)
    }

    private fun gradientFunction(
        theta: D2Array<Double>,
        X: D2Array<Double>,
        y: D2Array<Double>,
        a: Double,
        lr: Double
    ): D2Array<Double> {
        //number of samples
        val m = X.shape[0]

        val h = calculateOutput(X, theta)
        // difference between predicted and actual class
        val diff = h.minus(y)
        return X.transpose().dot(diff).times(lr).plus(a * theta.sum().toDouble()).times(1.0 / m)
    }

    private fun hasConverged(oldTheta: D2Array<Double>, newTheta: D2Array<Double>, epsilon: Double): Boolean {
        val diffSum = abs(oldTheta.minus(newTheta)).sum().toDouble()
        return (diffSum / oldTheta.shape[0] < epsilon)
    }

    private fun training(
        theta: D2Array<Double>,
        lr: Double,
        X: D2Array<Double>,
        y: D2Array<Double>,
        maxIterations: Int,
        epsilon: Double,
        a: Double
    ): D2Array<Double> {
        //set random seed
        var oldTheta = theta.copy()
        var newTheta = theta.copy()

        //optimalTheta = theta.dup()
        for (i in 1..maxIterations) {
            var gradients: D2Array<Double>? = gradientFunction(oldTheta, X, y, a, lr)

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

    private fun getData(): List<DoubleArray>{
        val a = ReadWriteFile()
        val mlData = a.read("", "mldata.csv")
        return mlData
    }

    private fun filterClassLabels(labels: D2Array<Double>, label: Double): D2Array<Double> {
        //returns an array with zeros where labels[i]!=label and ones where labels[i]==label
        val classn: D2Array<Double> = labels.copy()
        return classn.map { if ( it != label) 0.0 else it}
    }

    private fun countCorrectSamples(labels: D2Array<Double>, predictedLabels: DoubleArray): Double {
        var correctSamples = 0
        for (i in 0 until labels.size) {
            if (labels[i][0] == predictedLabels[i]) {
                correctSamples++
            }
        }
        return correctSamples.toDouble()
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