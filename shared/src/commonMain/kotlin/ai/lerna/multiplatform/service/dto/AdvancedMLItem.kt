package ai.lerna.multiplatform.service.dto
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class AdvancedMLItem {
    var embedding: Map<Int, D2Array<Double>>? = null
    var sensors: List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>? = null
    var attention: List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>? = null
    var lastlayer: List<Pair<List<D2Array<Double>>, List<D2Array<Double>>>>? = null
}