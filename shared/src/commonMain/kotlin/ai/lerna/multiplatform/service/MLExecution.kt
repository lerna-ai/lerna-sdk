package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeightsItem
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array



interface IMLExecution {
    suspend fun loadData(filename: String, deleteAfter: Boolean = true)
    fun prepareData(ml_id: Int, featureSize: Int): Boolean
    fun localML(ml_id: Int): Long
    fun addNoise(share: Float, scaling: Int, prediction: String): Any?
    fun setWeights(trainingWeights: GlobalTrainingWeightsItem)
    fun computeAccuracy(): Float
}