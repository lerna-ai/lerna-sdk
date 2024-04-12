package ai.lerna.multiplatform.service.advancedML
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import korlibs.datastructure.toIntList
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

interface Example

data class SimpleExample(
    val categFeatures: MutableList<Int>? = null,
    val numericalFeatures: MutableMap<Int, Float>? = null,
    val multiHotFeatures: List<MutableMap<Int, Int>>? = null,
    val sensors: D2Array<Float>? = null,
    var outputGold: DenseNDArray?
) : Example {
    /**
     * Dataset class for MovieLens dataset.
     * The dataset consists of categorical features, numerical features and one multi-hot categorical feature(genre).
     * Each possible feature value in categorical features is mapped to an integer.
     * For eg: If the dataset has a field "Location" with values ["New York", "San Francisco", "Chicago"],
     * then "New York" is mapped to 0, "San Francisco" is mapped to 1 and "Chicago" is mapped to 2.
     *
     * A numerical feature field is mapped to a single key/integer in the embedding table.
     * For eg: If the dataset has a field "Age", then a single key (say "3") is mapped to "Age".
     * The actual value of "Age" is mapped in the Map<3, 25.0> where 25.0 is the value of "Age" for a particular example.
     *
     * A multi-hot categorical feature, say Genre which can take multiple values, say(["Action", "Comedy", "Drama"])
     * is mapped to a list of keys/integers in the embedding table. Each possible value of this multi-hot feature is mapped to a key.
     * The value in the map indicates whether that particular feature is "present" or "absent" in the current example.
     * So for eg: if Genre field has a total feature set of ["Action", "Comedy", "Drama", "Romance", "Thriller"],
     * and in current example the Genre is ["Action", "Drama"], then the map will be as below:
     * {"4": 1, "5": 0, "6": 1, "7": 0, "8": 0} where 4, 5, 6, 7, 8 are the keys for ["Action", "Comedy", "Drama", "Romance", "Thriller"] respectively.
     */

    companion object {
        operator fun invoke(
            categoricalIndex: MutableList<Int>?, numericalIndex: MutableList<Int>?, numericalValues: MutableList<Float>?,
            multiHotFeaturesIndex: List<MutableList<Int>>?, multiHotFeaturesValues: List<MutableList<Float>>?,
            sensorData: FloatArray?, label: Int?) = SimpleExample(
            categFeatures = categoricalIndex,

            numericalFeatures = numericalIndex?.let { keys ->
                numericalValues?.let { values ->
                    if (keys.size != values.size) {
                        throw IllegalArgumentException("Numerical index and values sizes do not match")
                    } else {
                        mutableMapOf<Int, Float>().apply {
                            for (i in keys.indices) {
                                put(keys[i], values[i])
                            }
                        }
                    }
                }
            },

            multiHotFeatures = multiHotFeaturesIndex?.let { indexes ->
                multiHotFeaturesValues?.let { values ->
                    if (indexes.size != values.size) {
                        throw IllegalArgumentException("Multi-hot index and values sizes do not match")
                    } else {
                        val multiHotFeatures = mutableListOf<MutableMap<Int, Int>>()
                        for (i in indexes.indices) {
                            val multiHotFeature = mutableMapOf<Int, Int>()
                            for (j in indexes[i].indices) {
                                multiHotFeature[indexes[i][j]] = values[i][j].toInt()
                            }
                            multiHotFeatures.add(multiHotFeature)
                        }
                        multiHotFeatures
                    }
                }
            },

            // returns DenseNDArray
            sensors = sensorData?.let { mk.ndarray(sensorData, sensorData.size, 1)},


            outputGold = label?.let {DenseNDArray(storage= mk.ndarray(mk[mk[label.toFloat()]]))}
        )
    }

    fun addLabels(input:Float){
        outputGold = DenseNDArray(storage= mk.ndarray(mk[mk[input]]))
    }
}