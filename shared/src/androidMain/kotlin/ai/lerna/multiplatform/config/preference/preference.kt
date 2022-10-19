package ai.lerna.multiplatform.config.preference

import ai.lerna.multiplatform.config.KMMContext

const val SP_NAME = "ai.lerna.multiplatform"

actual fun KMMContext.putInt(key: String, value: Int) {
    getSpEditor().putInt(key, value).apply()
}

actual fun KMMContext.getInt(key: String, default: Int): Int {
    return  getSp().getInt(key, default )
}

actual fun KMMContext.getDouble(key: String, default: Double): Double {
    return  getSp().getFloat(key, default.toFloat()).toDouble()
}

actual fun KMMContext.putDouble(key: String, value: Double) {
    getSpEditor().putFloat(key, value.toFloat()).apply()
}

actual fun KMMContext.putString(key: String, value: String) {
    getSpEditor().putString(key, value).apply()
}

actual fun KMMContext.getString(key: String): String? {
    return  getSp().getString(key, null)
}

actual fun KMMContext.putArray(key: String, value: Array<String>) {
    getSpEditor().putStringSet(key, value.toSet()).apply()
}

actual fun KMMContext.getArray(key: String): Array<String>? {
    return getSp().getStringSet(key, null)?.toTypedArray()
}

actual fun KMMContext.putBool(key: String, value: Boolean) {
    getSpEditor().putBoolean(key, value).apply()
}

actual fun KMMContext.getBool(key: String, default: Boolean): Boolean {
    return getSp().getBoolean(key, default)
}

actual fun KMMContext.contains(value: String): Boolean {
    return getSp().contains(value)
}

private fun KMMContext.getSp() = getSharedPreferences(SP_NAME, 0)

private fun KMMContext.getSpEditor() = getSp().edit()