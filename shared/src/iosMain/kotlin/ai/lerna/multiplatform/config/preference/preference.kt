package ai.lerna.multiplatform.config.preference

import ai.lerna.multiplatform.config.KMMContext
import platform.Foundation.NSUserDefaults

actual fun KMMContext.putInt(key: String, value: Int) {
    NSUserDefaults.standardUserDefaults.setInteger(value.toLong(), key)
}

actual fun KMMContext.getInt(key: String, default: Int): Int {
    return NSUserDefaults.standardUserDefaults.integerForKey(key).toInt()
}

actual fun KMMContext.putDouble(key: String, value: Double) {
    NSUserDefaults.standardUserDefaults.setDouble(value, key)
}

actual fun KMMContext.getDouble(key: String, default: Double): Double {
    return NSUserDefaults.standardUserDefaults.doubleForKey(key)
}

actual fun KMMContext.putString(key: String, value: String) {
    NSUserDefaults.standardUserDefaults.setObject(value, key)
}

actual fun KMMContext.getString(key: String): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(key)
}

actual fun KMMContext.putArray(key: String, value: Array<String>) {
    NSUserDefaults.standardUserDefaults.setObject(value, key)
}

actual fun KMMContext.getArray(key: String): Array<String>? {
    return (NSUserDefaults.standardUserDefaults.stringArrayForKey(key) as List<String>?)?.toTypedArray()
}

actual fun KMMContext.putBool(key: String, value: Boolean) {
    NSUserDefaults.standardUserDefaults.setBool(value, key)
}

actual fun KMMContext.getBool(key: String, default: Boolean): Boolean {
    return NSUserDefaults.standardUserDefaults.boolForKey(key)
}

actual fun KMMContext.contains(value: String): Boolean {
    return NSUserDefaults.standardUserDefaults.objectForKey(value) != null
}