
package ai.lerna.multiplatform.service.advancedML
import ai.lerna.multiplatform.service.dto.AdvancedMLItem
import com.kotlinnlp.simplednn.core.functionalities.losses.LossCalculator
import com.kotlinnlp.simplednn.core.functionalities.updatemethods.UpdateMethod
import com.kotlinnlp.simplednn.core.optimizer.ParamsOptimizer
import com.kotlinnlp.simplednn.helpers.Trainer
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.utils.Shuffler


/**
 * A helper to train a feed-forward model with simple examples.
 *
 * @param model the neural model to train
 * @param updateMethod the update method helper (Learning Rate, ADAM, AdaGrad, ...)
 * @param lossCalculator a loss calculator
 * @param examples the training examples
 * @param epochs the number of training epochs
 * @param batchSize the size of each batch (default 1)
 * @param evaluator the helper for the evaluation (default null)
 * @param shuffler used to shuffle the examples before each epoch (with pseudo random by default)
 * @param verbose whether to print info about the training progress and timing (default = true)
 */
class CustomTrainer<NDArrayType: NDArray<NDArrayType>>(
    model: Any,
    updateMethod: UpdateMethod<*>,
    private val lossCalculator: LossCalculator,
    examples: List<SimpleExample>,
    testExamples: List<SimpleExample>,
    epochs: Int,
    batchSize: Int = 1,
    shuffler: Shuffler = Shuffler(),
    totalFeatures: Int,
    verbose: Boolean = true
) : Trainer<SimpleExample>(
    modelFilename = "",
    optimizers = listOf(ParamsOptimizer(updateMethod)),
    examples = examples,
    epochs = epochs,
    batchSize = batchSize,
    shuffler = shuffler,
    verbose = verbose
)
{

    /**
     * The neural processor that uses the model.
     */
    private val totalNumFeatures = totalFeatures

    private val sensorDim = if(examples.first().sensors!= null) examples.first().sensors!!.size else 0

    private val numEmbeddings = (if(examples.first().categFeatures!= null) 1 else 0) + (if(examples.first().numericalFeatures!= null) 1 else 0) + if(examples.first().multiHotFeatures!= null) examples.first().multiHotFeatures!!.size else 0

    var neuralProcessor = when(model) {
        // Specify the different model parameters here
        // If model contains both sensor data and feature data, then pass the sensor data size as well
        // If sensorDim != 0, then the model will have a sensor projection layer which gives an output of size sensorEmbeddingDim.
        // The default value of sensorEmbeddingDim is 16. If you want to change it, then pass the value in the constructor.

        "Attention" -> Attention(totalNumFeatures = this.totalNumFeatures, embeddingSize = 8, numEmbeddings = this.numEmbeddings, sensorDim = this.sensorDim)
        "LR" -> LR(totalNumFeatures = this.totalNumFeatures, embeddingSize = 8, numEmbeddings = this.numEmbeddings, sensorDim = this.sensorDim)
        else -> {
            println(model::class.simpleName)
            throw Exception("Unknown model type")}

    }

    fun reset()
    {   // Define the same model parameters here as well as above
        this.neuralProcessor = when(this.modelType) {
            "Attention" -> Attention(totalNumFeatures = this.totalNumFeatures, embeddingSize = 8, numEmbeddings = this.numEmbeddings, sensorDim = this.sensorDim)
            "LR" -> LR(totalNumFeatures = this.totalNumFeatures, embeddingSize = 8, numEmbeddings = this.numEmbeddings, sensorDim = this.sensorDim)
            else -> throw Exception("Unknown model type")
        }
        this.metrics.reset()
        this.lastLosses.clear()
        this.examplesCount = 0
    }

    private val modelType = model

    private val lastLosses: MutableList<Float> = mutableListOf()

    private var examplesCount = 0

    private val metrics = Metrics()

    private val totalExamples = examples.size

    private val testSet = testExamples


    /**
     * Learn from an example (forward + backward).
     *
     * @param example the example used to train the network
     */

    override fun learnFromExample(example: SimpleExample) {

        val output: DenseNDArray = this.neuralProcessor.forward(example)

        val errors: DenseNDArray = this.lossCalculator.calculateErrors(output, example.outputGold!!)
        val loss = this.lossCalculator.calculateLoss(output, example.outputGold!!)
        lastLosses.add(loss[0])
        metrics.append(example.outputGold!![0].toInt(), output[0])
        this.neuralProcessor.backward(errors)

        if (this.verbose) this.printProgressAndStats()
    }


    /**
     * Accumulate the errors of the model resulting after the call of [learnFromExample].
     */
    override fun accumulateErrors() {
        //TODO: The feedforward trainer accumulates copy of errors when batch size > 1 instead of reference,
        // investigate why, for now use the one without this condition?
        this.optimizers.single().accumulate(this.neuralProcessor.getParamsErrors(copy = this.batchSize > 1), copy = this.batchSize > 1)
//        this.optimizers.single().accumulate(this.neuralProcessor.getParamsErrors(copy = false), copy = false)
    }


    fun evaluate(examples: List<SimpleExample>) : Float {
        this.metrics.reset()
        examples.forEach{
            val prediction = this.neuralProcessor.forward(it)[0]
            metrics.append(it.outputGold!![0].toInt(), prediction)

        }
        val auc_score = this.metrics.aucScore()
        println("Test AUC score: ${this.metrics.aucScore()}")
        metrics.reset()
        return auc_score
    }






    private fun printProgressAndStats() {

        this.examplesCount++

        if (this.examplesCount % 10000 == 0)
            print(".")

        if (this.examplesCount % 100000 == 0) {
            println("After $examplesCount examples, Loss: ${this.lastLosses.average()}")
        }

        if (this.examplesCount == this.totalExamples) {
            println("Epoch loss: ${this.lastLosses.average()}")
            val auc = this.metrics.aucScore()
            println("Train AUC score: ${auc}")

            this.lastLosses.clear()
            this.metrics.reset()
            this.examplesCount = 0

            this.evaluate(this.testSet)
//            this.validateAndSaveModel()

        }
    }

    /**
     * Dump the model to file.
     */

    override fun dumpModel() {
        //TODO: Implement
    }

    fun getWeights(): AdvancedMLItem {
        when(this.neuralProcessor::class.simpleName) {
            "Attention" -> return (this.neuralProcessor as Attention).getWeights()
            "LR" -> return (this.neuralProcessor as LR).getWeights()
            else -> throw Exception("Unknown model type")
        }
    }

    fun setWeights(modelWeights: AdvancedMLItem) {
        when(this.neuralProcessor::class.simpleName) {
            "Attention" -> (this.neuralProcessor as Attention).setWeights(modelWeights)
            "LR" -> (this.neuralProcessor as LR).setWeights(modelWeights)
            else -> throw Exception("Unknown model type")
        }
    }
}