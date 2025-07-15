package ai.lerna.multiplatform

import ai.lerna.multiplatform.utils.addBoxed
import ai.lerna.multiplatform.utils.fft
import ai.lerna.multiplatform.utils.getAverageResultant
import ai.lerna.multiplatform.utils.getHistoricCalculations
import ai.lerna.multiplatform.utils.getSMA

actual class ModelData actual constructor(_historySize: Int) {
	internal actual val historySize = _historySize

	//private var proximity: Float = 0F
	private var mousePosition = arrayOf(0F, 0F)
	private var clickPosition = arrayOf(0F, 0F)
	internal actual var morning: Float = 0F
	internal actual var afternoon: Float = 0F
	internal actual var evening: Float = 0F
	internal actual var night: Float = 0F
	internal actual var weekday: Float = 0F
	internal actual var weekend: Float = 0F
	private var mousePositionHistoryX: ArrayDeque<Float> = ArrayDeque()
	private var mousePositionHistoryY: ArrayDeque<Float> = ArrayDeque()
	private var orientation: Float = 0F
	private var screenSize = arrayOf(0F, 0F)
	private var keyPressed: Float = 0F
	private var mouseClicks: Float = 0F
	private var customFeaturesArray: FloatArray = FloatArray(0)

	private var modelDataHistory: ArrayDeque<String> = ArrayDeque(0)

	private var mousePositionHistoryXFft: ArrayDeque<Float> = ArrayDeque()
	private var mousePositionHistoryYFft: ArrayDeque<Float> = ArrayDeque()

	internal actual fun setupCustomFeatureSize(customFeatureSize: Int) {
		if (customFeaturesArray.size != customFeatureSize && customFeatureSize != 0) {
			customFeaturesArray = FloatArray(customFeatureSize)
		}
	}

	internal actual fun updateCustomFeatures(customFeatures: FloatArray) {
		if (customFeaturesArray.size != customFeatures.size) {
			return
		}
		customFeaturesArray = customFeatures
	}

	internal fun setMousePosition(x: Float, y: Float) {
		mousePosition[0] = x
		mousePosition[1] = y
	}

	internal fun setOrientation(_orientation: Float) {
		orientation = _orientation
	}

	internal fun setScreenSize(x: Float, y: Float) {
		screenSize[0] = x
		screenSize[1] = y
	}

	fun setKeyPressedCounter(count: Float) {
		keyPressed = count
	}

	fun setClickCounter(count: Float) {
		mouseClicks = count
	}

	fun setClickPosition(x: Float, y: Float) {
		clickPosition[0] = x
		clickPosition[1] = y
	}

	internal actual fun clearHistory() {
		mousePositionHistoryX.clear()
		mousePositionHistoryY.clear()

		modelDataHistory.clear()
	}

	internal actual fun setDateTime(hour: Int, dayOfWeek: Int) {
		when (hour) {
			in 0..11 -> {
				morning = 1F
				afternoon = 0F
				evening = 0F
				night = 0F
			}

			in 12..15 -> {
				morning = 0F
				afternoon = 1F
				evening = 0F
				night = 0F
			}

			in 16..20 -> {
				morning = 0F
				afternoon = 0F
				evening = 1F
				night = 0F
			}

			in 21..23 -> {
				morning = 0F
				afternoon = 0F
				evening = 0F
				night = 1F
			}
		}

		if (dayOfWeek == 5 || // SATURDAY
			dayOfWeek == 6    // SUNDAY
		) {
			weekend = 1F
			weekday = 0F
		} else {
			weekend = 0F
			weekday = 1F
		}
	}


	internal actual fun setHistory(time: String) {
		try {
			mousePositionHistoryX.addBoxed(mousePosition[0])
			mousePositionHistoryY.addBoxed(mousePosition[1])

			mousePositionHistoryXFft = mousePositionHistoryX.fft()
			mousePositionHistoryYFft = mousePositionHistoryY.fft()

			modelDataHistory.addBoxed("$time,${toCsv()}", historySize)
		} catch (_: Exception) {
		}
	}

	internal actual fun historyToCsv(): String {
		return modelDataHistory
			.takeLast(historySize)
			.joinToString(separator = "\n", postfix = "\n") { it }
	}

	internal actual fun historyToCsv(sessionID: Int, successValue: String): String {
		return modelDataHistory
			.takeLast(historySize)
			.joinToString(
				prefix = "$sessionID,",
				separator = ",$successValue\n$sessionID,",
				postfix = ",$successValue\n"
			) { it }
	}

	internal actual fun toCsv(): String {
		return toArray().joinToString(separator = ",") { it.toString() }
	}

	internal actual fun toArray(): Array<Float> {
		val doubleArray: MutableList<Float> = ArrayList()

		try {
			// NEW: Total Num of features for mouse position = 4*12 + 3 = 51
			listOf(
				mousePositionHistoryX, mousePositionHistoryY,
				mousePositionHistoryXFft, mousePositionHistoryYFft
			).map { doubleArray.addAll(getHistoricCalculations(it)) }

			doubleArray.add(getSMA(mousePositionHistoryX, mousePositionHistoryY))
			doubleArray.add(getAverageResultant(mousePositionHistoryX, mousePositionHistoryY))
			doubleArray.add(getSMA(mousePositionHistoryXFft, mousePositionHistoryYFft))
		} catch (_: Exception) {
			// If any calculation failed, fill the rest values with zero
			for (i in doubleArray.size..50) doubleArray.add(0.0f)
		}


		doubleArray.add(mousePosition[0])
		doubleArray.add(mousePosition[1])

		doubleArray.add(morning)
		doubleArray.add(afternoon)
		doubleArray.add(evening)
		doubleArray.add(night)
		doubleArray.add(weekday)
		doubleArray.add(weekend)
		doubleArray.add(orientation)
		doubleArray.add(keyPressed)
		doubleArray.add(clickPosition[0])
		doubleArray.add(clickPosition[1])
		doubleArray.add(mouseClicks)
		doubleArray.addAll(customFeaturesArray.map { it })
		return doubleArray.toTypedArray()
	}

	internal actual fun historyToArray(): Array<FloatArray> {
		return modelDataHistory
			.takeLast(historySize)
			.map { line ->
				line.split(",")
					.filter { !it.contains("_") }
					.map { it.toFloat() }.toFloatArray()
			}
			.toTypedArray()
	}

	internal actual fun isHistoryNonEmpty(): Boolean {
		return modelDataHistory.isNotEmpty()
	}
}
