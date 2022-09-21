package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.service.dto.GlobalTrainingWeights
import ai.lerna.multiplatform.service.dto.TrainingAccuracy
import ai.lerna.multiplatform.service.dto.TrainingInference
import ai.lerna.multiplatform.service.dto.TrainingInferenceItem
import ai.lerna.multiplatform.service.dto.TrainingTasks
import ai.lerna.multiplatform.service.dto.TrainingWeights
import io.github.aakira.napier.Napier
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.http.
//import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.fasterxml.jackson.databind.JsonMappingException
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array


class FederatedLearningService(host: String, _requestQueue: RequestQueue, _token: String, _uniqueId: Long) {
    private val requestQueue = _requestQueue
    private val token = _token
    private var uniqueID = _uniqueId
    private val FL_API_URL = host
    private val mapper = JacksonConfiguration.newObjectMapper
    private lateinit var trainingTasks: TrainingTasks

    var newTrainingListener: ((TrainingTasks) -> Unit)? = null
    var submitWeightsListener: ((String) -> Unit)? = null
    var requestWeightsListener: ((GlobalTrainingWeights) -> Unit)? = null
    var submitAccuracyListener: ((String) -> Unit)? = null
    var submitInferenceListener: ((String) -> Unit)? = null
    var submitErrorListener: ((String) -> Unit)? = null

    fun setTrainingTaskResponse(trainingTasks: TrainingTasks) {
        this.trainingTasks = trainingTasks
    }

    fun isTrainingTaskReady(version: Long = 0): Boolean {
        return this::trainingTasks.isInitialized
                && trainingTasks.version!! > version
    }

    fun getTrainingTask(version: Long = 0): TrainingTasks? {
        if (isTrainingTaskReady(version)) {
            return trainingTasks
        }
        return null
    }

    fun requestNewTraining() {
        val stringRequest = StringRequest(
            Request.Method.GET,
            FL_API_URL + "training/new?token=" + token + "&deviceId=" + uniqueID,
            { response ->
                Napier.d("LernaFLService - requestNewTraining Response: $response for userID: $uniqueID")
                try {
                    trainingTasks = mapper.readValue(response, TrainingTasks::class)
                    newTrainingListener?.invoke(trainingTasks)
                } catch (e: JsonMappingException) {
                    Napier.d("LernaFLService - requestNewTraining Cannot deserialize response: " + e.message)
                    e.printStackTrace()
                    submitErrorListener?.invoke(e.message ?: "")
                }
            },
            { error ->
                Napier.d("LernaFLService - requestNewTraining Error Request: " + error.message)
                submitErrorListener?.invoke(error.message ?: "")
            })
        requestQueue.add(stringRequest)
    }

    fun submitWeights(
        jobId: Long,
        version: Long,
        datapoints: Long,
        deviceWeights: D2Array<Double>?
    ) {
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, FL_API_URL + "training/submitWeights?token=" + token,
            Response.Listener { response ->
                Napier.d("LernaFLService - submitWeights Response: $response for userID: $uniqueID and jobID: $jobId")
                submitWeightsListener?.invoke(response)
            },
            Response.ErrorListener { error ->
                Napier.d("LernaFLService - submitWeights Error Request: " + error.message)
                submitErrorListener?.invoke(error.message ?: "")
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                val request = TrainingWeights()
                request.jobId = jobId
                request.deviceId = uniqueID
                request.version = version
                request.datapoints = datapoints
                request.deviceWeights = deviceWeights
                return mapper.writeValueAsBytes(request)
            }
        }
        stringRequest.retryPolicy = HttpRequestRetry(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue.add(stringRequest)
    }

    fun requestNewWeights(version: Long) {
        val stringRequest = StringRequest(
            Request.Method.GET,
            FL_API_URL + "training/getNewWeights?token=" + token + "&version=" + version,
            { response ->
                Napier.d("LernaFLService - requestNewWeights Response: $response for userID: $uniqueID")
                if (response == null || response == "") {
                    Napier.d("LernaFLService - requestNewWeights empty response")
                    requestWeightsListener?.invoke(GlobalTrainingWeights())
                } else {
                    try {

                        val trainingWeightsResponse =
                            mapper.readValue(response, GlobalTrainingWeights::class)
                        //Log.d("LernaFLService", "sending the response version: ${trainingWeightsResponse.trainingWeights}")
                        requestWeightsListener?.invoke(trainingWeightsResponse)
                    } catch (e: JsonMappingException) {
                        Napier.d("LernaFLService - requestNewWeights Cannot deserialize response: " + e.message)
                        e.printStackTrace()
                        requestWeightsListener?.invoke(GlobalTrainingWeights())
                    }
                }
            },
            { error ->
                Napier.d("LernaFLService - requestNewWeights Error Request: " + error.message)
                requestWeightsListener?.invoke(GlobalTrainingWeights())
            })
        requestQueue.add(stringRequest)
    }

    fun submitAccuracy(mlId: Long, version: Long, accuracy: Double) {
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, FL_API_URL + "training/accuracy?token=" + token,
            Response.Listener { response ->
                Log.d("LernaFLService", "submitAccuracy Response: $response for userID: $uniqueID")
                submitAccuracyListener?.invoke(response)
            },
            Response.ErrorListener { error ->
                Napier.d("LernaFLService - submitAccuracy Error Request: " + error.message)
//				submitErrorListener?.invoke(error.message ?: "")
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                val request = TrainingAccuracy()
                request.mlId = mlId
                request.deviceId = uniqueID
                request.version = version
                request.accuracy = accuracy
                return mapper.writeValueAsBytes(request)
            }
        }
        requestQueue.add(stringRequest)
    }

    fun submitInference(version: Long, trainingInferenceItems: List<TrainingInferenceItem>, userIdentifier: String = "") {
        //Log.d("LernaFLService", "submitInference for token $token")
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, FL_API_URL + "training/inference?token=" + token,
            Response.Listener { response ->
                //		Log.d("LernaFLService", "submitInference Response: $response for userID: $uniqueID")
                submitInferenceListener?.invoke(response)
            },
            Response.ErrorListener { error ->
                Napier.d("LernaFLService - submitInference Error Request: " + error.message)
//				submitErrorListener?.invoke(error.message ?: "")
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                val request = TrainingInference()
                request.deviceId = uniqueID
                request.userIdentifier = userIdentifier
                request.version = version
                request.trainingInference = trainingInferenceItems
                return mapper.writeValueAsBytes(request)
            }
        }
        requestQueue.add(stringRequest)
    }
}