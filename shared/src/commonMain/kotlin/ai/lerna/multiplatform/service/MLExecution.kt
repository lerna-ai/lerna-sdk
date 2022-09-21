package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array



interface IMLExecution {
    fun loadData()
    fun prepareData(ml_id: Int)
    fun localML(ml_id: Int): Long
    fun addNoise(share: Double, scaling: Int, prediction: String): D2Array<Double>?
    fun setWeights(trainingWeights: GlobalTrainingWeightsItem)
    fun computeAccuracy(): Double
}