package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.utils.LogAwsUploaderImpl
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.util.date.*
import kotlinx.coroutines.runBlocking
import kotlin.native.concurrent.Worker

actual class FLWorkerInterface actual constructor(_context: KMMContext) {
	private val context = _context
	actual fun startFL(token: String, uniqueID: Long) {
		Napier.base(DebugAntilog())

		//IF background tasks are enabled, schedule flWorker every 12 hours
		//ELSE run it now and make sure it finishes by extending app's background execution time

		////////////////////////////////////////////////////////////////////
		// Example of flWorker running on iOS //////////////////////////////
		val flWorker = FLWorker(token, uniqueID)
		flWorker.setupStorage(context)
		val worker = Worker.start()
		worker.executeAfter(3000, performWorkLambda(token, uniqueID, flWorker))
		////////////////////////////////////////////////////////////////////
	}

	private fun performWorkLambda(token: String, uniqueID: Long, flWorker: FLWorker): () -> Unit = {
		runBlocking {
			try {
				flWorker.startFLSuspend()
			} catch (e:Exception) {
				Napier.e ("Lerna Worker - Failed $e")
				LogAwsUploaderImpl(token, FLWorker.FL_WORKER_VERSION).uploadFile(uniqueID,"",e.stackTraceToString(), GMTDate())
			}
		}
	}
}