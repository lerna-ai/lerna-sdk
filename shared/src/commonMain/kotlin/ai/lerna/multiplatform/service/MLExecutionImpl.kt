import ai.lerna.multiplatform.service.IMLExecution
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import ai.lerna.multiplatform.service.dto.TrainingTasks
import com.soywiz.korio.file.std.tempVfs
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
    private lateinit var trainLabels: Array<String>
    private lateinit var testLabels: Array<String>
    var thetaClass = HashMap<String, D2Array<Float>>()
    private val task = _task
    private lateinit var nextFeatures: List<FloatArray>
    private lateinit var nextLabels: List<Pair<Float, String>>


    override fun prepareData(ml_id: Int, featureSize: Int) {
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
        val train = mk.ndarray(trainDataFeatures.toTypedArray())
        val test = mk.ndarray(testDataFeatures.toTypedArray())

        trainLabels = trainDataLabels.toTypedArray()
        testLabels = testDataLabels.toTypedArray()

        val range=IntRange(1, trainDataFeatures[0].size-1)

        trainFeatures = train.slice<Float,D2,D2>(range, 1).transpose()
        trainFeatures = mk.ones<Float>(trainDataFeatures.size).cat(trainFeatures.flatten()).reshape(range.count()+1, trainDataFeatures.size)
        trainFeatures = trainFeatures.transpose()

        testFeatures = test.slice<Float,D2,D2>(range, 1).transpose()
        testFeatures = mk.ones<Float>(testDataFeatures.size).cat(testFeatures.flatten()).reshape(range.count()+1, testDataFeatures.size)
        testFeatures = testFeatures.transpose()


        Napier.d("LernaML - Data size: " + trainDataFeatures.size + " and " + testDataFeatures.size)
    }

    override fun localML(ml_id: Int): Long {
        if (thetaClass.isEmpty()) {
            Napier.e("LernaML - Cannot train without initializing the weights first")
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
                filterClassLabels(trainLabels, key),
                epoch,
                0.0001f,
                a!!.toFloat()
            )
        }

        return (trainLabels.size + testLabels.size).toLong()
    }

    override fun addNoise(share: Float, scaling: Int, prediction: String): D2Array<Float>? {
        return thetaClass[prediction]?.times(scaling.toFloat())?.plus(share)
    }


    override fun setWeights(trainingWeights: GlobalTrainingWeightsItem) {
        thetaClass.clear()
        trainingWeights.weights!!.forEach { (k, v) ->
            thetaClass[k] = v
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

    private fun predictLabels(testFeatures: D2Array<Float>): Array<String> {
        val outputs = HashMap<String, D2Array<Float>>()
        thetaClass.forEach { (k, v) ->
            outputs[k] = calculateOutput(testFeatures, v)
        }
        val result = Array(outputs.values.toList()[0].shape[0]){"failure"}

        for (i in 0 until outputs.values.toList()[0].shape[0]) {
            //Napier.i("predict "+ outputs["1.0"]!!.get(i,0).toString()+", "+ outputs["2.0"]!!.get(i,0).toString()+", "+ outputs["3.0"]!!.get(i,0).toString())
            var max = 0.0f //if more than 1 class, always pick the most probable one even if the probability is very low
            var value = "failure"
            if(thetaClass.size==1)
                max = 0.5f //give success only if confidence is more than 50% (in case we have only 1 class, i.e., success/failure)
            outputs.forEach { (k, _) ->
                if (outputs[k]!!.asD2Array()[i, 0] > max) {
                    max = outputs[k]!!.asD2Array()[i, 0]
                    value = k
                }
            }
            //Napier.i("chosen $i, $value")
            result[i] = value
        }

        return result
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


    override suspend fun loadData(filename: String, deleteAfter: Boolean){
        val mlData = tempVfs[filename].readLines().toList()
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
            tempVfs[filename].delete()
    }

    private fun filterClassLabels(labels: Array<String>, label: String): D2Array<Float> {
        //returns an array with zeros where labels[i]!=label and ones where labels[i]==label
        val classn: List<List<Float>> = labels.map { if ( it != label) listOf(0.0f) else listOf(1.0f)}
        return mk.ndarray(classn)
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

    private fun concat(A: Array<DoubleArray>, B: Array<DoubleArray>, vararg X: Array<DoubleArray>): D2Array<Double>? {
        val mkA = mk.ndarray(A).transpose()
        val mkB = mk.ndarray(B).transpose()
        if(mkA.shape[1]!=mkB.shape[1])
            return null
        var output = mkA.flatten().cat(mkB.flatten())
        var totalColumns = mkA.shape[0]+mkB.shape[0]
        for (list in X) {
            val mkX = mk.ndarray(list).transpose()
            if(mkA.shape[1]!=mkX.shape[1])
                return null
            output = output.cat(mkX.flatten())
            totalColumns += mkX.shape[0]
        }
        return output.reshape(totalColumns, mkA.shape[1]).transpose()
    }
}
