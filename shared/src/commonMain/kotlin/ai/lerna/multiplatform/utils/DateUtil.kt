package ai.lerna.multiplatform.utils

import io.ktor.util.date.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DateUtil {
	internal fun now(): String {
		return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toCustomDate()
	}

	internal fun nowGMT(): String {
		return GMTDate().toCustomDate()
	}

	private fun LocalDateTime.toCustomDate(): String = buildString {
		append(year.padZero(4))
		append("-${(month.ordinal + 1).padZero(2)}")
		append("-${dayOfMonth.padZero(2)}")
		append("_${hour.padZero(2)}.${minute.padZero(2)}.${second.padZero(2)}")
	}

	private fun GMTDate.toCustomDate(): String = buildString {
		append(year.padZero(4))
		append("-${(month.ordinal + 1).padZero(2)}")
		append("-${dayOfMonth.padZero(2)}")
		append("_${hours.padZero(2)}.${minutes.padZero(2)}.${seconds.padZero(2)}")
	}

	private fun Int.padZero(length: Int): String = toString().padStart(length, '0')
}