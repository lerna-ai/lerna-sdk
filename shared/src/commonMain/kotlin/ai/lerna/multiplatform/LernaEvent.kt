package ai.lerna.multiplatform

enum class LernaEvent(val value: Int) {
	SUCCESS(1),
	SUCCESS_ALPHA(2),
	SUCCESS_BETA(3),
	SUCCESS_GAMMA(4),
	SUCCESS_DELTA(5),
	SUCCESS_EPSILON(6),
	SUCCESS_ZETA(7),
	SUCCESS_ETA(8),
	SUCCESS_THETA(9),
	SUCCESS_IOTA(10);

	companion object {
		fun valueOf(value: Int) = LernaEvent.values().firstOrNull { it.value == value } ?: SUCCESS
	}
}
