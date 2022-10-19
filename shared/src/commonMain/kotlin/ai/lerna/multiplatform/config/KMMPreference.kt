package ai.lerna.multiplatform.config

import ai.lerna.multiplatform.config.preference.*

class KMMPreference(private val context: KMMContext) {

    fun put(key: String, value: Int) {
        context.putInt(key, value)
    }

    fun put(key: String, value: String) {
        context.putString(key, value)
    }

    fun put(key: String, value: Boolean) {
        context.putBool(key, value)
    }

    fun put(key: String, value: Double) {
        context.putDouble(key, value)
    }

    fun put(key: String, value: Array<String>) {
        context.putArray(key, value)
    }

    fun getInt(key: String, default: Int): Int
            =  context.getInt(key, default)

    fun getDouble(key: String, default: Double): Double
            =  context.getDouble(key, default)

    fun getArray(key: String) : Array<String>?
            =  context.getArray(key)

    fun getString(key: String) : String?
            =  context.getString(key)

    fun getBool(key: String, default: Boolean): Boolean =
        context.getBool(key, default)

    fun contains(value: String): Boolean =
        context.contains(value)

}
