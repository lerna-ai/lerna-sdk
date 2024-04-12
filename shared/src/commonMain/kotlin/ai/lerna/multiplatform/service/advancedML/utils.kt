package ai.lerna.multiplatform.service.advancedML
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.core.functionalities.losses.LossCalculator


fun <T> reshapeList(inputList: MutableList<T>, shape: List<Int>): List<MutableList<T>>? {
    val totalElements = shape[0] * shape[1]

    if (inputList.size != totalElements) {
        println("Error: The total number of elements in the input list doesn't match the specified shape.")
        return null
    }

    return inputList.chunked(shape[1]).map { it.toMutableList() }
}


class BinaryCELossCalculator : LossCalculator {
    override fun calculateLoss(output: DenseNDArray, outputGold: DenseNDArray): DenseNDArray {
        val groundTruth = outputGold.toFloatArray()[0]
        return if (groundTruth == 1.0f) -output.ln()
        else (-(-output.sum(-1.0f)).ln())
    }

    override fun calculateErrors(output: DenseNDArray, outputGold: DenseNDArray): DenseNDArray {
        val groundTruth = outputGold.toFloatArray()[0]
        val numeratorTerm = output.sum(-groundTruth)
        val denominatorTerm = output.prod(-output.sum(-1.0f))
        val errors = numeratorTerm.div(denominatorTerm)
        return errors
    }
}