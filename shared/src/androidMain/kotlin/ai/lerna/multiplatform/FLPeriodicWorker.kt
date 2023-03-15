package ai.lerna.multiplatform

import ai.lerna.multiplatform.utils.LogAwsUploaderImpl
import android.app.Application
import android.content.Context
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.soywiz.korio.android.withAndroidContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FLPeriodicWorker(_context: Context, workerParams: WorkerParameters) :
	ListenableWorker(_context, workerParams) {
	private val token = inputData.getString("token") ?: ""
	private val context = _context
	private val uniqueID = inputData.getLong("ID", 0L)
	private val flWorker = FLWorker(token.toString(), uniqueID)
	override fun startWork(): ListenableFuture<Result> {
		return CallbackToFutureAdapter.getFuture { completer ->
			flWorker.setupStorage(context.applicationContext as Application)

			CoroutineScope(Dispatchers.IO).launch {
				try {
					withAndroidContext(context) { flWorker.startFLSuspend() }
					completer.set(Result.success())
				} catch (e: Exception) {
					Napier.e("Lerna Worker - Failed $e")
					withAndroidContext(context) {
						LogAwsUploaderImpl(token, FLWorker.FL_WORKER_VERSION).uploadFile(uniqueID, "error_worker.txt", e.stackTraceToString())
					}
					completer.set(Result.failure())
				}
			}
		}
	}
}