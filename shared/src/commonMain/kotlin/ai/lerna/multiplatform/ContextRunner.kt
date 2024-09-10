package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1
import kotlin.reflect.KSuspendFunction2

expect class ContextRunner() {
	fun run(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>)
	fun run(context: KMMContext, strVal1:String, strVal2:String, runInContext: KSuspendFunction2<String, String, Unit>)
	fun runBlocking(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>)
	fun runBlocking(context: KMMContext, modelName:String, runPeriodic: KSuspendFunction1<String, Unit>)
	fun runBlocking(context: KMMContext, modelName:String, boolVal: Boolean, runPeriodic: KSuspendFunction2<String, Boolean, Unit>)
}
