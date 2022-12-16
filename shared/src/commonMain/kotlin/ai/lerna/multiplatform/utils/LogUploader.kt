package ai.lerna.multiplatform.utils

import io.ktor.util.date.*


interface LogUploader {
    fun uploadFile(uniqueID: Long, fileNameSuffix: String, fileContent: String, fileNameDate: GMTDate)
}
