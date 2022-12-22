package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.StorageImpl
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.util.concurrent.TimeUnit

actual class FLWorkerInterface actual constructor(_context: KMMContext) {
	private val context = _context
	private val storage = StorageImpl(context)

	actual fun startFL(token: String, uniqueID: Long) {
		Napier.base(DebugAntilog())
		val flConstraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.UNMETERED)
			//.setRequiresDeviceIdle(true)
			.build()

		val inputData = Data.Builder()
			.putString("token", token)
			.putLong("ID", uniqueID)
			.build()

		val flWorkRequest: PeriodicWorkRequest =
			PeriodicWorkRequestBuilder<FLPeriodicWorker>(12, TimeUnit.HOURS)
				.setInitialDelay(2, TimeUnit.SECONDS)
				.setConstraints(flConstraints)
				.setInputData(inputData)
				.build()

		if (storage.getVersion() != FLWorker.FL_WORKER_VERSION) {
			storage.putVersion(FLWorker.FL_WORKER_VERSION)
			WorkManager
				.getInstance(context)
				.enqueueUniquePeriodicWork("LernaFLWork", ExistingPeriodicWorkPolicy.REPLACE, flWorkRequest)
			Log.d("Lerna", "LernaFLWorker replaced")
		} else {
			WorkManager
				.getInstance(context)
				.enqueueUniquePeriodicWork("LernaFLWork", ExistingPeriodicWorkPolicy.KEEP, flWorkRequest)
		}
	}
}