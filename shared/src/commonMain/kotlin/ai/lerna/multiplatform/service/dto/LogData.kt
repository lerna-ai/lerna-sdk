package ai.lerna.multiplatform.service.dto

import kotlinx.serialization.Serializable

@Serializable
class LogData {
    var key: String = ""
    var token: String = ""
    var data: String = ""
}