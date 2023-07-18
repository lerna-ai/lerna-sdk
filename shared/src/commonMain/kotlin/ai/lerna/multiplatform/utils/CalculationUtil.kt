package ai.lerna.multiplatform.utils

import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.math.exp
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.operations.div
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.times

class CalculationUtil {
	internal fun calculateOutput(X: D2Array<Float>, theta: D2Array<Float>): D2Array<Float> {
		val z = X.dot(theta)
		return sigmoid(z)
	}

	internal fun sigmoid(Z: D2Array<Float>): D2Array<Float> {
		// S(Z) = 1 / ( 1 - e ^ (-Z))
		return 1.0f.div(
			Z.times(-1.0f)
				.exp()
				.plus(1.0f)
		)
	}
}