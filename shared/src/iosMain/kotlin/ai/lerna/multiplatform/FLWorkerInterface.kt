package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.runBlocking
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker

actual class FLWorkerInterface actual constructor(_context: KMMContext) {
	actual fun startFL(flWorker: FLWorker) {
		doInBackground {
			runBlocking {
				flWorker.startFLSuspend()
			}
		}
	}

	private fun <T> doInBackground(block: () -> T): T {
		val worker = Worker.start()
		val result = worker.execute(TransferMode.SAFE, { block }, { it.invoke() }).result
		worker.requestTermination()
		return result
	}
}