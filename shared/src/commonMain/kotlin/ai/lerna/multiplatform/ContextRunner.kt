package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1

expect class ContextRunner() {
	fun run(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>)
	fun runBlocking(context: KMMContext, modelName:String, runPeriodic: KSuspendFunction1<String, Unit>)
	fun runBlocking(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>)

}
