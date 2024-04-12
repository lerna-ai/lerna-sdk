package ai.lerna.multiplatform.service.advancedML
import com.kotlinnlp.simplednn.core.neuralprocessor.NeuralProcessor
import com.kotlinnlp.simplednn.core.neuralprocessor.feedforward.FeedforwardNeuralProcessor
import com.kotlinnlp.simplednn.core.optimizer.ParamsErrorsAccumulator
import com.kotlinnlp.simplednn.core.optimizer.ParamsErrorsList
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import korlibs.util.format

class BatchProcessor(override val propagateToInput: Boolean = true, override val id: Int = 0) :
    NeuralProcessor<List<SimpleExample>, List<DenseNDArray>, List<DenseNDArray>, DenseNDArray>
{
    val errorsAccumulator = ParamsErrorsAccumulator()

    val processorsPool = ProcessorPool(totalNumFeatures = 3529,
                                        embeddingSize = 16,
                                        numEmbeddings = 7,
                                       modelType = "attention")

    private var usedProcessors: MutableList<NeuralProcessor<SimpleExample, DenseNDArray, DenseNDArray, DenseNDArray>> = mutableListOf()
    override fun getInputErrors(copy: Boolean): DenseNDArray {
        TODO("Not yet implemented")
    }


    /**
     * @param copy whether the returned errors must be a copy or a reference
     *
     * @return the parameters errors
     */
    override fun getParamsErrors(copy: Boolean): ParamsErrorsList =
        this.errorsAccumulator.getParamsErrors(copy = copy)

    /**
     * Execute the forward of the input to the output, using a dedicated feed-forward processor for each array of the
     * batch.
     * This method must be used when the input layer is NOT a merge layer.
     *
     * @param input the input batch
     *
     * @return a list containing the output of each forwarded processor
     */
    override fun forward(input: List<SimpleExample>): List<DenseNDArray> =
        this.forward(input = input, continueBatch = false)

    /**
     * Execute the forward of the input to the output, using a dedicated feed-forward processor for each array of the
     * batch.
     * This method must be used when the input layer is NOT a merge layer.
     *
     * If [continueBatch] is `true`, the current forwarding batch is expanded with the given [input], otherwise a new
     * batch is started (the default).
     *
     * WARNING: [continueBatch] should not be `true` if a backward has been called before.
     *
     * @param input the input batch
     * @param continueBatch whether this batch is the continuation of the last forwarded one
     *
     * @return a list containing the output of each forwarded processor
     */
    fun forward(input: List<SimpleExample>, continueBatch: Boolean = false): List<DenseNDArray> =
        input.mapIndexed { i, values ->
            if (!continueBatch && i == 0) this.reset()
            this.forwardProcessor(values)
        }


    /**
     * Execute the backward for a single element of the input batch, given its output errors and its index within the
     * range of all the elements of the current batch.
     *
     * @param elementIndex the index of an element within the whole batch
     * @param outputErrors the output errors given element
     */
    fun backward(elementIndex: Int, outputErrors: DenseNDArray) {

        require(elementIndex in 0 until this.usedProcessors.size) {
            "The processor index exceeds the last index of the used processors."
        }

        this.processorBackward(processor = this.usedProcessors[elementIndex], outputErrors = outputErrors)
    }

    /**
     * Execute the backward for all the elements of the batch, given the output errors.
     *
     * @param outputErrors the output errors of the whole batch
     */
    override fun backward(outputErrors: List<DenseNDArray>) {

        require(outputErrors.size == this.usedProcessors.size) {
            "Number of errors (%d) does not reflect the number of used processors (%d)".format(
                outputErrors.size, this.usedProcessors.size)
        }

        this.usedProcessors.zip(outputErrors).forEach { (processor, errors) ->
            this.processorBackward(processor = processor, outputErrors = errors)
        }
    }

    /**
     * Execute the forward of the input of a new element to the output, instantiating a new dedicated feed-forward
     * processor.
     * This method must be used when the input layer is NOT a merge layer.
     *
     * @param input the input array
     *
     * @return the output array
     */
    private fun forwardProcessor(input: SimpleExample): DenseNDArray =
        this.processorsPool.getItem().let { this.usedProcessors.add(it); it.forward(input) }

    /**
     * Execute the forward of the input of a new element to the output, instantiating a new dedicated feed-forward
     * processor.
     * This method must be used when the input layer is a merge layer.
     *
     * @param input the input array
     *
     * @return the output array
     */

    /**
     * Reset the current batch.
     */
    private fun reset() {

        this.processorsPool.releaseAll()
        this.usedProcessors.clear()
        this.errorsAccumulator.clear()
    }

    /**
     * Execute the backward of a given [processor], given its output [outputErrors].
     *
     * @param processor a processor used for the current batch
     * @param outputErrors the output errors
     */
    private fun processorBackward(processor: NeuralProcessor<SimpleExample, DenseNDArray, DenseNDArray, DenseNDArray>,
                                  outputErrors: DenseNDArray) {

        processor.backward(outputErrors = outputErrors)

        this.errorsAccumulator.accumulate(processor.getParamsErrors(copy = false))
    }
}


