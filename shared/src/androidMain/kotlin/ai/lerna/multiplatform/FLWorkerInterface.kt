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
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.util.concurrent.TimeUnit

actual class FLWorkerInterface actual constructor(_context: KMMContext) {
	private val context = _context
	private val storage = StorageImpl(context)
	actual fun startFL() {

		Napier.base(DebugAntilog())
		val flConstraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.UNMETERED)
			//.setRequiresDeviceIdle(true)
			.build()

		val inputData = Data.Builder()
			.putString("token", "632523a5-bdf1-4241-8ec0-f8c8cd666050")
			.putLong("ID", 123L)
			.build()

		val flWorkRequest: PeriodicWorkRequest =
			PeriodicWorkRequestBuilder<FLPeriodicWorker>(12, TimeUnit.HOURS)
				.setInitialDelay(2, TimeUnit.SECONDS)
				.setConstraints(flConstraints)
				.setInputData(inputData)
				.build()

		WorkManager
			.getInstance(context)
			.enqueueUniquePeriodicWork("LernaFLWork", ExistingPeriodicWorkPolicy.REPLACE, flWorkRequest)

	}
}