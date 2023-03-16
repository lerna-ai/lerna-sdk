package ai.lerna.multiplatform.utils

interface LogUploader {
    suspend fun uploadFile(uniqueID: Long, fileNameSuffix: String, fileContent: String)
}
