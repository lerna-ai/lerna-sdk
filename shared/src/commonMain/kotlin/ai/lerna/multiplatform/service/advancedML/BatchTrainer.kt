package ai.lerna.multiplatform.service.advancedML
import com.kotlinnlp.simplednn.core.functionalities.activations.Softmax
import com.kotlinnlp.simplednn.core.functionalities.losses.SoftmaxCrossEntropyCalculator
import com.kotlinnlp.simplednn.core.functionalities.losses.LossCalculator
import com.kotlinnlp.simplednn.core.functionalities.updatemethods.UpdateMethod
import com.kotlinnlp.simplednn.core.layers.StackedLayersParameters
import com.kotlinnlp.simplednn.core.neuralprocessor.feedforward.FeedforwardNeuralProcessor
import com.kotlinnlp.simplednn.core.optimizer.ParamsOptimizer
import com.kotlinnlp.simplednn.helpers.Trainer
import com.kotlinnlp.simplednn.core.neuralprocessor.NeuralProcessor
import com.kotlinnlp.simplednn.helpers.Statistics
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.utils.Shuffler
import com.kotlinnlp.utils.Timer
import korlibs.io.lang.format
import kotlinx.coroutines.runBlocking



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
internal class BatchTrainer<NDArrayType: NDArray<NDArrayType>>(
    model: Any,
    updateMethod: UpdateMethod<*>,
    private val lossCalculator: LossCalculator,
    examples: List<SimpleExample>,
    testExamples: List<SimpleExample>,
    epochs: Int,
    batchSize: Int = 1,
    shuffler: Shuffler = Shuffler(),
    verbose: Boolean = true
) : Trainer<SimpleExample>(
    modelFilename = "",
    optimizers = listOf(ParamsOptimizer(updateMethod)),
    examples = examples,
    epochs = epochs,
    batchSize = batchSize,
    shuffler = shuffler,
    verbose = verbose
) {

    /**
     * The neural processor that uses the model.
     */

    private val neuralProcessor = BatchProcessor()

    private val lastLosses: MutableList<Double> = mutableListOf()

    private var examplesCount = 0

    private val timer = Timer()

    private val metrics = Metrics()

    private val totalExamples = examples.size

    private val testSet = testExamples


    override fun trainEpoch() {
        val examplesIterator: Iterator<SimpleExample> = this.buildExamplesIterator()

        while (examplesIterator.hasNext())
        {

//            if (this.counter.exampleCount % this.batchSize == 0) // A new batch starts
//                this.newBatch()
            this.newBatch()

            this.newExample() // !! must be called after newBatch() !!

            val batchExamples = mutableListOf<SimpleExample>()
            for (i in 0 until this.batchSize) {
                if (examplesIterator.hasNext()) batchExamples.add(examplesIterator.next())
            }

            this.trainBatch(batchExamples)
        }
    }

    fun trainBatch(examples: List<SimpleExample>)
    {
        val batchOutput: List<DenseNDArray> = this.neuralProcessor.forward(examples)

        val batchErrors: List<DenseNDArray> = this.lossCalculator.calculateErrors(batchOutput, examples.map { it.outputGold!! })

        val batchLosses: List<DenseNDArray> = batchOutput.zip(examples).map { (output, example) ->
            this.lossCalculator.calculateLoss(output, example.outputGold!!)}

        val batchLoss = batchLosses.map { it[0] }.average()
        lastLosses.add(batchLoss)

        examples.mapIndexed{
                index, example -> metrics.append(example.outputGold!![0].toInt(), batchOutput[index][0])}

        this.neuralProcessor.backward(batchErrors)

        this.accumulateErrors()

        this.optimizers.forEach { it.update() }

        if (this.verbose) this.printProgressAndStats()

    }



    /**
     * Accumulate the errors of the model resulting after the call of [learnFromExample].
     */
    override fun accumulateErrors() {
        //TODO: The feedforward trainer accumulates copy of errors when batch size > 1 instead of reference,
        // investigate why, for now use the one without this condition
//        this.optimizers.single().accumulate(this.neuralProcessor.getParamsErrors(copy = this.batchSize > 1), copy = this.batchSize > 1)
        this.optimizers.single().accumulate(this.neuralProcessor.getParamsErrors(copy = false), copy = false)
    }



//    fun evaluate(examples: List<SimpleExample>) {
//        examples.forEach{
//            val prediction = this.neuralProcessor.forward(it)[0]
//            metrics.append(it.outputGold[0].toInt(), prediction)
//
//        }
//        println("Test AUC score: ${this.metrics.aucScore()}")
//        metrics.reset()
//    }


    fun printProgressAndStats() {

        this.examplesCount += this.batchSize
        this.examplesCount = minOf(this.totalExamples, this.examplesCount)

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


        }
    }

    /**
     * Dump the model to file.
     */

    override fun dumpModel() {
        //TODO: Implement
    }

    override fun learnFromExample(example: SimpleExample) {
        TODO("Not needed in batched training")
    }

}
