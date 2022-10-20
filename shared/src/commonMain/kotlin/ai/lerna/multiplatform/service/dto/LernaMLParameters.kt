package ai.lerna.multiplatform.service.dto
import kotlinx.serialization.Serializable

@Serializable
class LernaMLParameters {
    var normalization: Float? = null

    var iterations = 0

    var learningRate: Float? = null

    var dimensions = 0

    var dataSplit: Float? = null
}