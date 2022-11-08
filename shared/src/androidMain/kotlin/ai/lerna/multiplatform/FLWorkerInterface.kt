package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import com.soywiz.korio.android.withAndroidContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

actual class FLWorkerInterface actual constructor(_context: KMMContext) {
	private val context = _context
	actual fun startFL(flWorker: FLWorker) {
		CoroutineScope(Dispatchers.IO).launch {
			withAndroidContext(context) {
				flWorker.startFLSuspend()
			}
		}
	}
}