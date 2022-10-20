package ai.lerna.multiplatform.config

import android.content.Context
import java.util.UUID.randomUUID
import kotlin.math.abs

actual class userID {
     actual fun getUniqueId(context: KMMContext): Int {
        val prefID = "LernaUniqueID"
        //val context = KMMContext()
        val sharedPref = KMMPreference(context)
        val uniqueID =
            sharedPref.getInt(prefID, abs(randomUUID().toString().hashCode()))
        if (!sharedPref.contains(prefID)) {
            sharedPref.put(prefID, uniqueID)
        }
        return uniqueID
    }
}