package ai.lerna.multiplatform

import MLExecution
import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.FederatedLearningService
import ai.lerna.multiplatform.service.FileUtil
import ai.lerna.multiplatform.service.Storage
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.service.WeightsManager
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.MpcResponse
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.cacheVfs
import com.soywiz.korio.stream.writeString
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ones
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class FLWorker {
	// ToDo: Update FL Service configuration
	private val federatedLearningService = FederatedLearningService("https://api.dev.lerna.ai:7357/api/v2/", "632523a5-bdf1-4241-8ec0-f8c8cd666050", 123L)
	private lateinit var flWorkerInterface : FLWorkerInterface
	private val weightsManager = WeightsManager()
	private val fileUtil = FileUtil()
	private val scaling = 100000
	private var weightsVersion = -1L
	private var taskVersion = -1
	private val uniqueID = 0L
	private lateinit var storage: Storage
	private lateinit var context: KMMContext

	fun setupStorage(kmmContext: KMMContext) {
		context = kmmContext
		storage = StorageImpl(kmmContext)
		flWorkerInterface = FLWorkerInterface(kmmContext)
		weightsManager.setupStorage(storage)
	}

	fun startFL() {
		flWorkerInterface.startFL(this)
	}

	suspend fun startFLSuspend() = run {
		Napier.base(DebugAntilog())
		Napier.d("App Version: ${storage.getVersion()}", null, "LernaFL")
		val trainingTask = federatedLearningService.requestNewTraining() ?: return

		Napier.d("Task Version: ${trainingTask.version.toString()}", null, "LernaFL")

		taskVersion = trainingTask.version!!.toInt()
		var globalWeights : GlobalTrainingWeights? = null
		if (weightsManager.updateWeights() == "Success") {
			globalWeights = storage.getWeights()
			if (globalWeights != null) {
				weightsVersion = globalWeights.version
			}
		}

		val fileSize = fileUtil.mergeFiles(storage)

		if (fileSize < 500000L) {
			Napier.d("Not enough data for training", null, "LernaFL")
			//logUploader.uploadLogcat(uniqueID, "logcatf.txt")
			return;
		}

		var successes = 0
		//For each ML task
		val ml = MLExecution(trainingTask)
		ml.loadData()

		for (i in trainingTask.trainingTasks!!.indices) {
			ml.prepareData(i)
			ml.setWeights(globalWeights!!.trainingWeights!![i])
			if (weightsVersion > 1) {
				Napier.d("Computing accuracy of version $weightsVersion and task $i", null, "LernaFL")
				federatedLearningService.submitAccuracy(globalWeights!!.trainingWeights!![i].mlId!!, weightsVersion, ml.computeAccuracy())
			}
			//Train locally
			val size = ml.localML(i)

			if (size == -1L) {
				//logUploader.uploadLogcat(uniqueID, "logcat_errf.txt")
				continue
			}

			val newWeights: HashMap<String, D2Array<Float>> = HashMap()
			globalWeights!!.trainingWeights!![i].weights?.forEach {
				newWeights[it.key] = ml.thetaClass[ml.mapping(it.key)]!!
			}
			globalWeights!!.trainingWeights!![i].weights = newWeights.toMap()

			Napier.d("Computing accuracy of local model for task $i", null, "LernaFL")
			ml.computeAccuracy()

			//For each prediction/job
			globalWeights!!.trainingWeights!![i].weights!!.forEach { (key, _) ->
				try {
					val share = getNoise(uniqueID, key, size, i)!!.share!!.toFloat()
					val weights = ml.addNoise(share, scaling, key)
					val jobId = trainingTask.trainingTasks!![i].jobIds!![key]!!
					Napier.d("Submitting noisy weights for job $jobId", null, "LernaFL")

					val submitedWeights = federatedLearningService.submitWeights(jobId, taskVersion.toLong(), size, weights!!)
					if (submitedWeights != null) {
						successes++
						if (successes == (globalWeights!!.trainingWeights!![0].weights!!.size * trainingTask.trainingTasks!!.size)) { //assuming every task has the same number of jobs
							// Upload to AWS S3 implementation - Start
//							val fileNameDate = LocalDateTime.now(ZoneOffset.UTC).format(dateFormatter)
//							val temp = storage.getSuccesses()?.toList()?.sorted()
//							val baos = ByteArrayOutputStream()
//							temp?.forEach {
//								val row = if (it.split(',').size > 6)
//									it.split(',')[0] + "," + it.split(',')[1] + "," + it.split(',')[2] + "," + it.split(',')[4] + "," + it.split(',')[5] + "," + it.split(',')[6] + "\r\n"
//								else
//									it.split(',')[0] + "," + it.split(',')[1] + "," + it.split(',')[2] + "," + it.split(',')[4] + "\r\n"
//								baos.write(row.toByteArray())
//							}
//							val mlFile = context.openFileInput("mldata.csv")

//							logUploader.uploadFile(uniqueID, "inference.csv", baos.toByteArray(), fileNameDate)
//							if (fileSize > 0) {
//								logUploader.uploadFile(uniqueID, "mldata.csv", mlFile, fileNameDate)
//							}
//							logUploader.uploadLogcat(uniqueID, "logcatf.txt", fileNameDate)

							// Upload to AWS S3 implementation - End
							storage.putWeights(globalWeights)
							storage.putLastTraining(taskVersion)
							storage.putSize(storage.getSize() + fileSize.toInt())

//							FileUtils.cleanDirectory(context.filesDir)
							Napier.d("Cleaned up directory", null, "LernaFL")
						}
					}
				} catch (ex: Exception) {
					//logUploader.uploadLogcat(uniqueID, "logcat_errf.txt")
				}
			}
		}
	}

	private fun getNoise(uniqueID: Long, key: String, size: Long, i: Int): MpcResponse {
		//ToDo: Add implementation
		return MpcResponse()
	}
}