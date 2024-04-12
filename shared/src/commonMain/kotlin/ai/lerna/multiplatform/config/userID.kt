package ai.lerna.multiplatform.config

import com.russhwolf.settings.contains
import korlibs.io.util.UUID
import kotlin.math.abs

class UserID {
    fun getUniqueId(context: KMMContext): Int {
        val prefID = "LernaUniqueID"
        val sharedPref = KMMPreference(context)
        val uniqueID =
            sharedPref.getInt(prefID, abs(UUID.randomUUID().toString().hashCode()))
        if (!sharedPref.contains(prefID)) {
            sharedPref.putInt(prefID, uniqueID)
        }
        return uniqueID
    }
}