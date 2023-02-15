package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.utils.LogAwsUploaderImpl
import io.github.aakira.napier.Napier
import io.ktor.util.date.*
import kotlinx.coroutines.runBlocking
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow

actual class FLWorkerInterface actual constructor(_context: KMMContext) {

	// To SDK developer - put it somewhere with constants of your SDK

	private val taskIdentifier = "ai.lerna.kmm.fl"

	private val context = _context
	private lateinit var flWorker : FLWorker
	private var token = ""
	private var uniqueID = -1L

	actual fun startFL(token: String, uniqueID: Long) {
		this.token = token
		this.uniqueID = uniqueID
		flWorker = FLWorker(token, uniqueID)
		flWorker.setupStorage(context)

		// For clear previously planned tasks
		BGTaskScheduler.sharedScheduler.cancelAllTaskRequests()

		BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
			identifier = taskIdentifier,
			usingQueue = null, // when pass null it's background Queue by default
			launchHandler = { bgTask ->
				Napier.d("=== Scheduled task started ===")
				runWorker(bgTask) // Call worker run
			}
		)

		scheduleRefreshTask()
	}

	private fun scheduleRefreshTask() {
		Napier.d("=== Setup task scheduler ===")
		val bgTaskRequest = BGProcessingTaskRequest(identifier = taskIdentifier)

		bgTaskRequest.requiresExternalPower = true
		bgTaskRequest.requiresNetworkConnectivity = true
		bgTaskRequest.earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(
		secs = (60 * 60 * 12).toDouble()
		//	secs = (15).toDouble() // minimize for 15 seconds for test
		)

		try {
			BGTaskScheduler.sharedScheduler.submitTaskRequest(bgTaskRequest, error = null)
			Napier.d("=== Scheduler setup is success. ===")
			printScheduledTasks()
		} catch (e: Exception) {
			Napier.e("=== Scheduler setup failed ===", throwable = e)
			LogAwsUploaderImpl(token, FLWorker.FL_WORKER_VERSION).uploadFile(uniqueID, "", e.stackTraceToString(), GMTDate())
			runWorker(null)
		}
	}

	private fun runWorker(bgTask: BGTask?) {
		Napier.d("=== Worker started with bgTask: $bgTask ===")
		runBlocking {
			try {
				flWorker.startFLSuspend()
				Napier.d("=== Worker task completed ===")
				bgTask?.setTaskCompletedWithSuccess(true)
				scheduleRefreshTask()
			} catch (e: Exception) {
				Napier.d("=== Worker task failed ===")
				LogAwsUploaderImpl(token, FLWorker.FL_WORKER_VERSION).uploadFile(uniqueID, "", e.stackTraceToString(), GMTDate())
				bgTask?.setTaskCompletedWithSuccess(false)
				scheduleRefreshTask()
			}
		}
	}

	// Method for testing demo
	private fun printScheduledTasks() {
		BGTaskScheduler.sharedScheduler.getPendingTaskRequestsWithCompletionHandler { tasks ->
			Napier.d("=== Scheduled tasks list: $tasks ===")
		}
	}
}