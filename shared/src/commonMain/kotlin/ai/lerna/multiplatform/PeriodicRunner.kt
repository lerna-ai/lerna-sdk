package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction0


class PeriodicRunner {
	private var runPeriodicFlag = false
	fun run(context: KMMContext, initDelay: Long, runPeriodic: KSuspendFunction0<Unit>) {
		runPeriodicFlag = true
		CoroutineScope(Dispatchers.Default).launch {
			delay(initDelay)
			while (runPeriodicFlag) {
				runPeriodic()
				delay(500)
			}
		}
	}

	fun stop() {
		runPeriodicFlag = false
	}
}
