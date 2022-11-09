package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.runBlocking
import kotlin.native.concurrent.Worker

actual class FLWorkerInterface actual constructor(_context: KMMContext) {
	actual fun startFL(flWorker: FLWorker) {
		val worker = Worker.start()
		worker.executeAfter(3000, performWorkLambda(flWorker))
	}

	private fun performWorkLambda(flWorker: FLWorker): () -> Unit = {
		runBlocking {
			flWorker.startFLSuspend()
		}
	}
}