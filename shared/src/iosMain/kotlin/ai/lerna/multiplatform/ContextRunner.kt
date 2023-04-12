package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1


actual class ContextRunner actual constructor() {
	actual fun run(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>) {
		CoroutineScope(Dispatchers.Default).launch {
			runPeriodic()
		}
	}

	actual fun runBlocking(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>) {
		runBlocking {
			runPeriodic()
		}
	}
	actual fun runBlocking(context: KMMContext, modelName: String, runPeriodic: KSuspendFunction1<String, Unit>) {
		runBlocking {
			runPeriodic(modelName)
		}
	}
}
