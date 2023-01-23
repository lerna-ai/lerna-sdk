package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlin.reflect.KSuspendFunction0

expect class PeriodicRunner() {
	fun run(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>)
	fun stop()
}