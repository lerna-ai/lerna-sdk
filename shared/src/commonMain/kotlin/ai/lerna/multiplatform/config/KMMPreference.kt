package ai.lerna.multiplatform.config

import ai.lerna.multiplatform.config.preference.*

internal class KMMPreference(private val context: KMMContext) {

    internal fun put(key: String, value: Int) {
        context.putInt(key, value)
    }

    internal fun put(key: String, value: String) {
        context.putString(key, value)
    }

    internal fun put(key: String, value: Boolean) {
        context.putBool(key, value)
    }

    internal fun put(key: String, value: Float) {
        context.putFloat(key, value)
    }

    internal fun put(key: String, value: Array<String>) {
        context.putArray(key, value)
    }

    internal fun getInt(key: String, default: Int): Int
            =  context.getInt(key, default)

    internal fun getFloat(key: String, default: Float): Float
            =  context.getFloat(key, default)

    internal fun getArray(key: String) : Array<String>?
            =  context.getArray(key)

    internal fun getString(key: String) : String?
            =  context.getString(key)

    internal fun getBool(key: String, default: Boolean): Boolean =
        context.getBool(key, default)

    internal fun contains(value: String): Boolean =
        context.contains(value)

}
