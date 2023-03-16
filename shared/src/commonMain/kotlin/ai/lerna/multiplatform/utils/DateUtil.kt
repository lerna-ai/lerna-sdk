package ai.lerna.multiplatform.utils

import io.ktor.util.date.*

class DateUtil {
	internal fun now(): String {
		return GMTDate().toCustomDate()
	}

	private fun GMTDate.toCustomDate(): String = buildString {
		append(year.padZero(4))
		append("-${(month.ordinal + 1).padZero(2)}")
		append("-${dayOfMonth.padZero(2)}")
		append("_${hours.padZero(2)}.${minutes.padZero(2)}.${seconds.padZero(2)}")
	}

	private fun Int.padZero(length: Int): String = toString().padStart(length, '0')
}