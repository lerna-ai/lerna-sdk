package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.utils.LogAwsUploaderImpl
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import org.w3c.dom.Worker

actual class FLWorkerInterface actual constructor(_context: KMMContext) {

    private val context = _context
    private val uploadPrefix = StorageImpl(_context).getUploadPrefix()
    private lateinit var flWorker: FLWorker
    private var token = ""
    private var uniqueID = -1L
    actual fun startFL(token: String, uniqueID: Long) {
        this.token = token
        this.uniqueID = uniqueID
        flWorker = FLWorker(token, uniqueID)
        flWorker.setupStorage(context)

        try {
            CoroutineScope(Dispatchers.Default).launch {
                flWorker.startFLSuspend()
                Napier.d("=== Worker task completed ===")
            }
//            val worker_fn = { worker: Worker ->
//                Napier.d("=== FLWorkerInterface start worker ===")
//                GlobalScope.promise {
//                    Napier.d("=== FLWorkerInterface launch in global scope ===")
//                    worker.runCatching { flWorker.startFLSuspend() }
//                }
//            }
//            Napier.d("=== Worker $worker_fn task completed ===")

        } catch (e: Exception) {
            Napier.d("=== Worker task failed ===")
            val worker_fn = { worker: Worker ->
                GlobalScope.promise {
                    worker.runCatching {
                        LogAwsUploaderImpl(
                            token,
                            FLWorker.FL_WORKER_VERSION
                        ).uploadFile(
                            uniqueID,
                            uploadPrefix,
                            "error_worker.txt",
                            e.stackTraceToString()
                        )
                    }
                }
            }
        }
    }
}
