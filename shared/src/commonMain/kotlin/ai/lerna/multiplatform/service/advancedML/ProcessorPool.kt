package ai.lerna.multiplatform.service.advancedML
import com.kotlinnlp.simplednn.core.neuralprocessor.NeuralProcessor
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.utils.ItemsPool

class ProcessorPool(val totalNumFeatures: Int,
                    val embeddingSize: Int,
                    val numEmbeddings: Int,
                    val modelType: String):
    ItemsPool<NeuralProcessor<SimpleExample, DenseNDArray, DenseNDArray, DenseNDArray>>()
{

    override fun itemFactory(id: Int): NeuralProcessor<SimpleExample, DenseNDArray, DenseNDArray, DenseNDArray> =
        if(modelType == "attention") {
            Attention(totalNumFeatures, embeddingSize, numEmbeddings, propagateToInput = true, id=id)
        }
    else if (modelType == "embeddingLR") {
        LR(totalNumFeatures, embeddingSize, numEmbeddings, propagateToInput = true, sensorDim = 0, id = id)
    }
    else {
        throw Exception("Model type not supported")
    }

}