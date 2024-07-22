package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.StorageImpl
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.aakira.napier.Napier
import java.util.concurrent.TimeUnit

actual class FLWorkerInterface actual constructor(_context: KMMContext) {
	private val context = _context
	private val storage = StorageImpl(context)

	actual fun startFL(token: String, uniqueID: Long) {
		val flConstraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.UNMETERED)
			.setRequiresDeviceIdle(true)
			.setRequiresBatteryNotLow(true)
			.build()

		val inputData = Data.Builder()
			.putString("token", token)
			.putLong("ID", uniqueID)
			.putString("uploadPrefix", storage.getUploadPrefix())
			.build()

		val flWorkRequest: PeriodicWorkRequest =
			PeriodicWorkRequestBuilder<FLPeriodicWorker>(12, TimeUnit.HOURS)
				.setInitialDelay(12, TimeUnit.HOURS)
				.setConstraints(flConstraints)
				.setInputData(inputData)
				.build()

		if (storage.getVersion() != FLWorker.FL_WORKER_VERSION) {
			storage.putVersion(FLWorker.FL_WORKER_VERSION)
			WorkManager
				.getInstance(context)
				.enqueueUniquePeriodicWork("LernaFLWork", ExistingPeriodicWorkPolicy.UPDATE, flWorkRequest)
			Napier.d("LernaFLWorker replaced", null, "Lerna")
		} else {
			WorkManager
				.getInstance(context)
				.enqueueUniquePeriodicWork("LernaFLWork", ExistingPeriodicWorkPolicy.KEEP, flWorkRequest)
		}
	}
}