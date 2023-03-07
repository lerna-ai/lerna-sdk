package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction0


actual class PeriodicRunner actual constructor() {
	private var runPeriodicFlag = false
	actual fun run(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>) {
		runPeriodicFlag = true
		CoroutineScope(Dispatchers.Default).launch {
			delay(2000)
			while (runPeriodicFlag) {
				runPeriodic()
				delay(2000)
			}
		}
	}

	actual fun stop() {
		runPeriodicFlag = false
	}
}
