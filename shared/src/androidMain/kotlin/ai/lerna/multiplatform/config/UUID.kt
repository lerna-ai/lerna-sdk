package ai.lerna.multiplatform.config

import ai.lerna.multiplatform.config.preference.contains
import ai.lerna.multiplatform.config.preference.getInt
import ai.lerna.multiplatform.config.preference.putInt
import java.util.UUID.randomUUID
import kotlin.math.abs

actual class userID {
     actual fun getUniqueId(): Int {
        val prefID = "LernaUniqueID"
        val context = KMMContext()
        //val sharedPref = Context.getSharedPreferences("ai.lerna.android", Context.MODE_PRIVATE)
        val uniqueID =
            context.getInt(prefID, abs(randomUUID().toString().hashCode()))
        if (!context.contains(prefID)) {
            context.putInt(prefID, uniqueID)
        }
        return uniqueID
    }
}