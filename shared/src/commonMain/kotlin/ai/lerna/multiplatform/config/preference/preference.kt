package ai.lerna.multiplatform.config.preference

import ai.lerna.multiplatform.config.KMMContext

expect fun KMMContext.putInt(key: String, value: Int)

expect fun KMMContext.getInt(key: String, default: Int): Int

expect fun KMMContext.putDouble(key: String, value: Double)

expect fun KMMContext.getDouble(key: String, default: Double): Double

expect fun KMMContext.putArray(key: String, value: Array<String>)

expect fun KMMContext.getArray(key: String): Array<String>?

expect fun KMMContext.putString(key: String, value: String)

expect fun KMMContext.getString(key: String) : String?

expect fun KMMContext.putBool(key: String, value: Boolean)

expect fun KMMContext.getBool(key: String, default: Boolean): Boolean

expect fun KMMContext.contains(value: String): Boolean