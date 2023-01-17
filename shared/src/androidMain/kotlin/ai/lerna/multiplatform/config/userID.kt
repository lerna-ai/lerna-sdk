package ai.lerna.multiplatform.config

import java.util.UUID.randomUUID
import kotlin.math.abs

actual class UserID actual constructor() {
     actual fun getUniqueId(context: KMMContext): Int {
        val prefID = "LernaUniqueID"
        val sharedPref = KMMPreference(context)
        val uniqueID =
            sharedPref.getInt(prefID, abs(randomUUID().toString().hashCode()))
        if (!sharedPref.contains(prefID)) {
            sharedPref.put(prefID, uniqueID)
        }
        return uniqueID
    }
}