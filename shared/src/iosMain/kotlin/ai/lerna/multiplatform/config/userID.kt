package ai.lerna.multiplatform.config

import platform.Foundation.NSUUID

import kotlin.math.abs

actual class userID {
    actual fun getUniqueId(context:KMMContext): Int {
        val prefID = "LernaUniqueID"
        val sharedPref = KMMPreference(context)
        val uniqueID =
            sharedPref.getInt(prefID, abs(NSUUID().UUIDString().toString().hashCode()))
        if (!sharedPref.contains(prefID)) {
            sharedPref.put(prefID, uniqueID)
        }
        return uniqueID
    }
}