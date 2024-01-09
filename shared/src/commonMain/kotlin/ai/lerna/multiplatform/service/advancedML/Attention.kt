package ai.lerna.multiplatform.service.advancedML

import ai.lerna.multiplatform.service.dto.AdvancedMLItem
import com.kotlinnlp.simplednn.core.embeddings.LernaEmbeddingsMap
import com.kotlinnlp.simplednn.core.functionalities.activations.Sigmoid
import com.kotlinnlp.simplednn.core.layers.LayerInterface
import com.kotlinnlp.simplednn.core.layers.LayerType
import com.kotlinnlp.simplednn.core.layers.StackedLayersParameters
import com.kotlinnlp.simplednn.core.neuralprocessor.NeuralProcessor
import com.kotlinnlp.simplednn.core.neuralprocessor.embeddingsprocessor.LernaEmbeddingsProcessor
import com.kotlinnlp.simplednn.core.neuralprocessor.feedforward.FeedforwardNeuralProcessor
import com.kotlinnlp.simplednn.core.optimizer.ParamsErrorsAccumulator
import com.kotlinnlp.simplednn.core.optimizer.ParamsErrorsList
import com.kotlinnlp.simplednn.deeplearning.attention.multihead.MultiHeadAttentionNetwork
import com.kotlinnlp.simplednn.deeplearning.attention.multihead.MultiHeadAttentionParameters
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import korlibs.io.lang.assert
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import org.jetbrains.kotlinx.multik.ndarray.operations.toListD2

class Attention(
    // totalNumFeatures is the full size of the input domain (i.e. sum total number of unique feature values in the dataset)
    val totalNumFeatures: Int,

    // embeddingSize is the size of the embedding vector for each feature. It should be a small
    // number like 8, 16 or max 32.
    val embeddingSize: Int,

    // numEmbeddings is the total number of embeddings corresponding to each of the feature field
    val numEmbeddings: Int,

    // Input size of the sensor data
    val sensorDim: Int = 0,

    // sensorEmbeddingDim is the size of the projected sensor data after passing through an MLP layer
    // It should be a smaller number than the actual size of the sensor data, for eg: (8, 16 or 32)
    // so that the model can learn the representation of the sensor data.
    // If no value is provided in constructor and sensorDim > 0, the default of 16 is used.
    var sensorEmbeddingDim: Int = 16,

    override val propagateToInput: Boolean = true,
    override var id: Int = 0,
): NeuralProcessor<SimpleExample, DenseNDArray, DenseNDArray, DenseNDArray>{
    val embeddingTable =
        LernaEmbeddingsMap<Int>(num_features = totalNumFeatures, size = embeddingSize)

    init {
        embeddingTable.load_init()
    }

    val errorsAccumulator = ParamsErrorsAccumulator()

    val embeddingProcessor = LernaEmbeddingsProcessor<Int>(embeddingTable, dropout = 0.0)

    val attentionLayerParams = MultiHeadAttentionParameters(
        inputSize = embeddingSize, attentionSize = 32, attentionOutputSize = 32,
        numOfHeads = 2
    )

    val attentionLayer =
        MultiHeadAttentionNetwork(model = attentionLayerParams, propagateToInput = true)


    // If sensor data exists then create a sensor layer
    val sensorLayerParameters: StackedLayersParameters? = if (sensorDim > 0) {
        StackedLayersParameters(
            // input layer
            LayerInterface(size = sensorDim, type = LayerType.Input.Dense),
            // output sensor layer
            LayerInterface(
                size = sensorEmbeddingDim,
                activationFunction = Sigmoid,
                connectionType = LayerType.Connection.Feedforward
            )
        )
    }
    else {
        // If sensor data is not provided, then sensorEmbeddingDim is set to 0 overriding the default value of 16
        sensorEmbeddingDim = 0
        null
    }

    val sensorLayer: FeedforwardNeuralProcessor<DenseNDArray>? = if (sensorLayerParameters != null)
    {
        FeedforwardNeuralProcessor(model = sensorLayerParameters, dropout = 0.0, propagateToInput = false)
    }
    else null

    val finalLayerParams = StackedLayersParameters(
        LayerInterface(
            size = numEmbeddings * embeddingSize + sensorEmbeddingDim,
            type = LayerType.Input.Dense
        ),
        LayerInterface(
            size = 1,
            activationFunction = Sigmoid,
            connectionType = LayerType.Connection.Feedforward
        )
    )

    val finalLayer = FeedforwardNeuralProcessor<DenseNDArray>(
        model = finalLayerParams,
        dropout = 0.0,
        propagateToInput = true
    )


    override fun forward(input: SimpleExample): DenseNDArray {
        val embedding: List<DenseNDArray> = embeddingProcessor.forward(input.categFeatures, input.numericalFeatures, input.multiHotFeatures).map { it as DenseNDArray }

        val concatInput: D2Array<Double> = if (embedding.isNotEmpty())
        {
            val attentionOutput = attentionLayer.forward(embedding)
            val flatArray = mk.ndarray(mk[attentionOutput.map { it.storage.toList()}]).reshape(numEmbeddings * embeddingSize, 1)

            // If no sensor data is provided, then the input is the attention output
            if (input.sensors == null) {
                flatArray
            }
            // If sensor data is provided, then pass it through the sensor layer and concatenate it with the attention output
            else
            {
                assert (input.sensors.shape[0] == sensorDim) {"The input sensor data size must match the sensorDim provided in the model constructor."}
                val sensorOutput = sensorLayer!!.forward(DenseNDArray(storage = input.sensors))
                mk.ndarray(mk[flatArray.toList() + sensorOutput.storage.toList()]).transpose(1, 0)
            }
        }

        // If no embedding is provided, then the input is the sensor data
        else
        {   assert(input.sensors != null) {"No input provided in the example. Please provide either categorical, numerical, multi-hot features or sensor data"}
            assert (input.sensors!!.shape[0] == sensorDim) {"The input sensor data size must match the sensorDim provided in the model constructor."}
            val sensorOutput = sensorLayer!!.forward(DenseNDArray(storage = input.sensors))
            sensorOutput.storage
        }

        val output = finalLayer.forward(DenseNDArray(storage = concatInput))

        return output
    }

    override fun backward(outputErrors: DenseNDArray){

        this.errorsAccumulator.clear()

        finalLayer.backward(outputErrors)
        this.errorsAccumulator.accumulate(finalLayer.getParamsErrors(copy = false))

        val concatErrors = finalLayer.getInputErrors(copy=true)

        if (numEmbeddings > 0) {
            // separate the gradients of dim (numEmbeddings * embeddingSize) for backpropagation into the attention layer
            val attentionErrors: List<DenseNDArray> = concatErrors.storage[0 until numEmbeddings * embeddingSize].reshape(numEmbeddings, embeddingSize).
            toListD2().map { DenseNDArray(mk.ndarray(mk[it]).reshape(embeddingSize, 1))}

            if (sensorDim > 0) {
                // separate the gradients of dim (sensorDim) for backpropagation into the sensor layer
                val endIndex = concatErrors.storage.shape[0]
                val sensorErrors = concatErrors.storage[numEmbeddings * embeddingSize until endIndex].reshape(sensorEmbeddingDim, 1)
                sensorLayer!!.backward(DenseNDArray(storage = sensorErrors as D2Array<Double>))
                this.errorsAccumulator.accumulate(sensorLayer.getParamsErrors(copy = false))
            }

            attentionLayer.backward(attentionErrors)
            this.errorsAccumulator.accumulate(attentionLayer.getParamsErrors(copy = false))

            val embeddingErrors = attentionLayer.getInputErrors(copy = true)
            embeddingProcessor.backward(embeddingErrors)
            this.errorsAccumulator.accumulate(embeddingProcessor.getParamsErrors(copy = false))
        }

        // if no embeddings, all the gradients are for the sensor layer
        else
        {
            sensorLayer!!.backward(concatErrors)
            this.errorsAccumulator.accumulate(sensorLayer.getParamsErrors(copy = false))
        }

    }

    override fun getInputErrors(copy: Boolean): DenseNDArray {
        TODO(" not needed")
    }

    override fun getParamsErrors(copy: Boolean): ParamsErrorsList =
        this.errorsAccumulator.getParamsErrors(copy = copy)


    fun getWeights() : AdvancedMLItem {
        val weights = AdvancedMLItem()

        // Return the embedding table weights of type Map<Int, D2Array<Double>>
        weights.embedding = embeddingTable.getParams() //as Map<Int, D2Array<Double>>

        // Return the Sensor layer weights of type List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>
        if (sensorDim > 0)
            weights.sensors = sensorLayerParameters?.getParams()
        // Return the Attention layer weights of type List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>
        weights.attention = attentionLayerParams.getParams()

        // Return the final layer weights of type List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>
        weights.lastlayer = finalLayerParams.getParams()
        return weights
    }

    fun setWeights(modelWeights: AdvancedMLItem) {
        val embeddingWeights = modelWeights.embedding
        if(embeddingWeights!=null)
            embeddingTable.setParams(embeddingWeights)

        val sensorsParams = modelWeights.sensors// as List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>
        if(sensorsParams!=null)
            sensorLayerParameters?.setParams(sensorsParams)

        val attentionParams = modelWeights.attention// as List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>
        if(attentionParams!=null)
            attentionLayerParams.setParams(attentionParams)

        val layerParams = modelWeights.lastlayer// as List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>
        if(layerParams!=null)
            finalLayerParams.setParams(layerParams)
    }


}