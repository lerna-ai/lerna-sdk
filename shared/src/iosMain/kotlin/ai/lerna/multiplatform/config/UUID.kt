package ai.lerna.multiplatform.config

import ai.lerna.multiplatform.config.preference.getInt
import ai.lerna.multiplatform.config.preference.putInt
import ai.lerna.multiplatform.config.preference.contains
import platform.Foundation.NSUUID

import kotlin.math.abs

actual class userID {
    actual fun getUniqueId(): Int {
        val prefID = "LernaUniqueID"
        val context = KMMContext()
        val uniqueID =
            context.getInt(prefID, abs(NSUUID().UUIDString().toString().hashCode()))
        if (!context.contains(prefID)) {
            context.putInt(prefID, uniqueID)
        }
        return uniqueID
    }
}