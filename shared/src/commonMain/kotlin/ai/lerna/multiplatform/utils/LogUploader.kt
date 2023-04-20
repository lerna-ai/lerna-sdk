package ai.lerna.multiplatform.utils

interface LogUploader {
    suspend fun uploadFile(uniqueID: Long, uploadPrefix: String, fileNameSuffix: String, fileContent: String)
}
