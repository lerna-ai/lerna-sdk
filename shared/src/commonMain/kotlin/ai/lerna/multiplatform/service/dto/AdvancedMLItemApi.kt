package ai.lerna.multiplatform.service.dto
import kotlinx.serialization.Serializable
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

@Serializable
class AdvancedMLItemApi {
    var embedding: Map<Int, Array<FloatArray>>? = null
    var sensors: List<Pair<List<Array<FloatArray>>, List<Array<FloatArray>>>>? = null
    var attention: List<Pair<List<Array<FloatArray>>, List<Array<FloatArray>>>>? = null
    var lastlayer: List<Pair<List<Array<FloatArray>>, List<Array<FloatArray>>>>? = null
}