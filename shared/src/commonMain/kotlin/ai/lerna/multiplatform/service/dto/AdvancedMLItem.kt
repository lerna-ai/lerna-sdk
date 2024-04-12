package ai.lerna.multiplatform.service.dto
import kotlinx.serialization.Serializable
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array


class AdvancedMLItem {
    var embedding: Map<Int, D2Array<Float>>? = null
    var sensors: List<Pair<List<D2Array<Float>>, List<D2Array<Float>>>>? = null
    var attention: List<Pair<List<D2Array<Float>>, List<D2Array<Float>>>>? = null
    var lastlayer: List<Pair<List<D2Array<Float>>, List<D2Array<Float>>>>? = null
}