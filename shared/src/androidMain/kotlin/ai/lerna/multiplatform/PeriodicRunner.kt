package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import korlibs.io.android.withAndroidContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.reflect.KSuspendFunction0


actual class PeriodicRunner actual constructor() {
	private var runPeriodicFlag = false
	actual fun run(context: KMMContext, initDelay: Long, runPeriodic: KSuspendFunction0<Unit>) {
		runPeriodicFlag = true
		CoroutineScope(Dispatchers.Default).launch {
			withAndroidContext(context) {
				delay(initDelay)
				while (runPeriodicFlag) {
					runPeriodic()
					delay(500)
				}
			}
		}
	}

	actual fun stop() {
		runPeriodicFlag = false
	}
}