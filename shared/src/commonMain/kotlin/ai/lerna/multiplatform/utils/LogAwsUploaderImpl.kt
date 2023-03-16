package ai.lerna.multiplatform.utils

import ai.lerna.multiplatform.LernaConfig
import ai.lerna.multiplatform.getPlatform
import ai.lerna.multiplatform.service.dto.LogData
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


class LogAwsUploaderImpl(_token: String, _version: Int) : LogUploader {
    private val token = _token
    private val version = _version
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    override suspend fun uploadFile(uniqueID: Long, fileNameSuffix: String, fileContent: String) {
        try {
            withContext(CoroutineScope(Dispatchers.Default).coroutineContext) {
                val request = LogData()
                request.path = LernaConfig.UPLOAD_PREFIX
                request.key = uniqueID.toString() + "/" + version.padZero(4) + "_" + DateUtil().now() + "_" + getPlatform().name + "_" + fileNameSuffix
                request.token = token
                request.data = fileContent
                val response = client.post("https://path/to/upload/file") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
                if (response.status != HttpStatusCode.OK) {
                    Napier.e("LogAwsUploader - Log response error: ${response.bodyAsText()}", null, "LernaLog")
                }
            }
        } catch (e: Exception) {
            Napier.e("LogAwsUploader: Failed to upload file $fileNameSuffix.", null, "LernaLog")
        }
    }

    private fun Int.padZero(length: Int): String = toString().padStart(length, '0')
}