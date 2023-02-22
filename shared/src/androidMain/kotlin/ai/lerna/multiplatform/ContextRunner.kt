package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import com.soywiz.korio.android.withAndroidContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KSuspendFunction0

actual class ContextRunner actual constructor() {
	actual fun run(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>) {
		CoroutineScope(Dispatchers.Default).launch {
			withAndroidContext(context) {
				runPeriodic()
			}
		}
	}

	actual fun runBlocking(context: KMMContext, runPeriodic: KSuspendFunction0<Unit>) {
		runBlocking {
			withAndroidContext(context) {
				runPeriodic()
			}
		}
	}
}
