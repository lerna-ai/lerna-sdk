package ai.lerna.multiplatform.utils

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
                ignoreUnknownKeys = true
            })
        }
    }

    override suspend fun uploadFile(
        uniqueID: Long,
        uploadPrefix: String,
        fileNameSuffix: String,
        fileContent: String
    ) {
        try {

            val request = LogData()
            request.path = uploadPrefix
            request.key =
                uniqueID.toString() + "/" + version.padZero(4) + "_" + DateUtil().nowGMT() + "_" + getPlatform().name.replace("/","-") + "_" + fileNameSuffix
            request.token = token
            request.data = fileContent
            val response =
                client.post("https://4chh1yguvg.execute-api.eu-west-1.amazonaws.com/release/logfile") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            if (response.status != HttpStatusCode.OK) {
                Napier.e(
                    "LogAwsUploader - Log response error: ${response.bodyAsText()}",
                    null,
                    "LernaLog"
                )
            }

    } catch (e: Exception)
    {
        Napier.e("LogAwsUploader: Failed to upload file $fileNameSuffix.", null, "LernaLog")
    }
}

    private fun Int.padZero(length: Int): String = toString().padStart(length, '0')
}