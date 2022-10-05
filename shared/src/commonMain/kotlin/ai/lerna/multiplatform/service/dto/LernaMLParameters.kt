package ai.lerna.multiplatform.service.dto
import kotlinx.serialization.Serializable

@Serializable
class LernaMLParameters {
    var normalization: Double? = null

    var iterations = 0

    var learningRate: Double? = null

    var dimensions = 0

    var dataSplit: Double? = null
}