package ai.lerna.multiplatform

import ai.lerna.multiplatform.utils.DateUtil

expect class ModelData(_historySize: Int = 1) {
	internal val historySize: Int
	internal var morning: Float
	internal var afternoon: Float
	internal var evening: Float
	internal var night: Float
	internal var weekday: Float
	internal var weekend: Float

	internal fun setupCustomFeatureSize(customFeatureSize: Int)

	internal fun updateCustomFeatures(customFeatures: FloatArray)


	internal fun clearHistory()

	internal fun setDateTime(hour: Int, dayOfWeek: Int)

	internal fun setHistory(time: String = DateUtil().now())

	internal fun historyToCsv(): String

	internal fun historyToCsv(sessionID: Int, successValue: String): String

	internal fun toCsv(): String

	internal fun toArray(): Array<Float>

	internal fun historyToArray(): Array<FloatArray>

	internal fun isHistoryNonEmpty(): Boolean
}
