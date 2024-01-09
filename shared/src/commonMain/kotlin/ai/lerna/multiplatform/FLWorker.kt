package ai.lerna.multiplatform

import ai.lerna.multiplatform.service.MLExecution
import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.service.ConfigService
import ai.lerna.multiplatform.service.FederatedLearningService
import ai.lerna.multiplatform.service.FileUtil
import ai.lerna.multiplatform.service.MpcService
import ai.lerna.multiplatform.service.Storage
import ai.lerna.multiplatform.service.StorageImpl
import ai.lerna.multiplatform.service.WeightsManager
import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.MpcResponse
import ai.lerna.multiplatform.utils.LogAwsUploaderImpl
import korlibs.io.file.std.tempVfs
import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

class FLWorker(_token: String, _uniqueID: Long) {
	private val token = _token
	private val uniqueID = _uniqueID
	private lateinit var flWorkerInterface: FLWorkerInterface
	private val weightsManager = WeightsManager(token, uniqueID)
	private val fileUtil = FileUtil()
	private val scaling = 100000
	private var weightsVersion = -1L
	private var taskVersion = -1
	private lateinit var storage: Storage
	private lateinit var context: KMMContext
	private lateinit var federatedLearningService: FederatedLearningService

	internal companion object {
		const val FL_WORKER_VERSION = 1    // Increment on worker changes
	}

	fun setupStorage(kmmContext: KMMContext) {
		context = kmmContext
		storage = StorageImpl(kmmContext)
		federatedLearningService = FederatedLearningService(storage.getFLServer(), token, uniqueID)
		flWorkerInterface = FLWorkerInterface(kmmContext)
		weightsManager.setupStorage(storage)
	}

	suspend fun startFLSuspend() = run {
		if (!ConfigService(context, token, uniqueID).updateConfig()) {
			Napier.d("The Lerna token cannot be validated, Library disabled", null, "LernaFL")
			return
		}
		Napier.d("App Version: ${storage.getVersion()}", null, "LernaFL")
		val trainingTask = federatedLearningService.requestNewTraining(storage.getClasses()) ?: return

		Napier.d("Task Version: ${trainingTask.version.toString()}", null, "LernaFL")

		taskVersion = trainingTask.version!!.toInt()
		var globalWeights: GlobalTrainingWeights? = null
		if (weightsManager.updateWeights() == "Success") {
			globalWeights = storage.getWeights()
			if (globalWeights != null) {
				weightsVersion = globalWeights.version
			}
		}

		var success = true
		val ml = MLExecution(trainingTask)

		//checkpoint
		val fileSize = fileUtil.mergeFiles(storage, "mldata.csv", "sensorLog")

		/*
		 * Check session number to allow FL execution
		 */
		if (storage.getSessionID() < storage.getTrainingSessionsThreshold()) {
			Napier.d("Not enough data for training. Total file size: $fileSize, session ${storage.getSessionID()}.", null, "LernaFL")
			return
		}


		if (storage.getUploadDataEnabled()) {
			LogAwsUploaderImpl(token, FL_WORKER_VERSION)
				.uploadFile(
					uniqueID,
					storage.getUploadPrefix(),
					"mldata.csv",
					tempVfs["mldata.csv"].readString())
		}
		ml.loadData("mldata.csv")
		storage.putSessionID(0)

		//For each ML task - if we want different files for different mls, we need to put everything after the checkpoint inside the loop and use the parameter i
		for (i in trainingTask.trainingTasks!!.indices) {

			if(!ml.prepareData(i, globalWeights!!.trainingWeights!!.find { it.mlId ==  trainingTask.trainingTasks!![i].mlId}!!.weights!!.entries.first().value.size)) {
				Napier.d("Skipping task $i because of not enough data or wrong data size", null, "LernaFL")
				continue
			}
			ml.setWeights(globalWeights.trainingWeights!!.find { it.mlId ==  trainingTask.trainingTasks!![i].mlId}!!)
			if (weightsVersion > 1) {
				Napier.d("Computing accuracy of version $weightsVersion and task $i", null, "LernaFL")
				federatedLearningService.submitAccuracy(trainingTask.trainingTasks!![i].mlId!!, weightsVersion, ml.computeAccuracy())
			}
			//Train locally
			val size = ml.localML(i)

			if (size == -1L) {
				//logUploader.uploadLogcat(uniqueID, "logcat_errf.txt")
				continue
			}

			val newWeights: HashMap<String, D2Array<Float>> = HashMap()
			globalWeights.trainingWeights!!.find { it.mlId ==  trainingTask.trainingTasks!![i].mlId}!!.weights?.forEach {
				newWeights[it.key] = ml.thetaClass[it.key]!!
			}
			globalWeights.trainingWeights!!.find { it.mlId ==  trainingTask.trainingTasks!![i].mlId}!!.weights = newWeights.toMap() //will this work now?

			Napier.d("Computing accuracy of local model for task $i", null, "LernaFL")
			ml.computeAccuracy()

			//For each prediction/job
			globalWeights.trainingWeights!!.find { it.mlId ==  trainingTask.trainingTasks!![i].mlId}!!.weights!!.forEach { (key, _) ->
				try {
					val jobId = trainingTask.trainingTasks!![i].jobIds!![key]!!
					val share = getNoise(uniqueID, size, jobId)!!.Share!!.toFloat()
					val weights = ml.addNoise(share, scaling, key)
					Napier.d("Submitting noisy weights for job $jobId", null, "LernaFL")

					val submittedWeights = federatedLearningService.submitWeights(jobId, taskVersion.toLong(), size, weights!!)
					if(submittedWeights==null)
						success = false
				} catch (ex: Exception) {
					LogAwsUploaderImpl(token, FL_WORKER_VERSION).uploadFile(uniqueID, storage.getUploadPrefix(), "error_fl.txt", ex.stackTraceToString())
				}
			}
		}
		if(success) {
			storage.putWeights(globalWeights)
			storage.putLastTraining(taskVersion)
			storage.putSize(storage.getSize() + fileSize.toInt())
//			FileUtils.cleanDirectory(context.filesDir)
//			Napier.d("Cleaned up directory", null, "LernaFL")
		}
	}

//	private fun getWeightSize(): Int {
//		val weights = storage.getWeights()?.trainingWeights?.get(0)?.weights ?: return -1
//		val firstKey = weights.keys.first()
//		return weights[firstKey]?.size ?: -1
//	}

	private suspend fun getNoise(
		uniqueID: Long,
		size: Long,
		job_id: Long?
	): MpcResponse? {
		//if (!federatedLearningService.isTrainingTaskReady()) {
		//	return null
		//}
		if (job_id == null) {
			return null
		}
		Napier.d("Retrieving noise share from MPC...", null, "LernaFL")
		return MpcService(storage.getMPCServer(), token).lerna(job_id, uniqueID, size)
	}
}