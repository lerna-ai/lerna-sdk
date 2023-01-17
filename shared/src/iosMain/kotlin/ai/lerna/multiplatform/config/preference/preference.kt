package ai.lerna.multiplatform.config.preference

import ai.lerna.multiplatform.config.KMMContext
import platform.Foundation.NSUserDefaults

actual fun KMMContext.putInt(key: String, value: Int) {
    NSUserDefaults.standardUserDefaults.setInteger(value.toLong(), key)
}

actual fun KMMContext.getInt(key: String, default: Int): Int {
    return if(NSUserDefaults.standardUserDefaults.objectForKey(key) != null)
        NSUserDefaults.standardUserDefaults.integerForKey(key).toInt()
    else
        default
}

actual fun KMMContext.putFloat(key: String, value: Float) {
    NSUserDefaults.standardUserDefaults.setFloat(value, key)
}

actual fun KMMContext.getFloat(key: String, default: Float): Float {
    return if(NSUserDefaults.standardUserDefaults.objectForKey(key) != null)
        NSUserDefaults.standardUserDefaults.floatForKey(key)
    else
        default
}

actual fun KMMContext.putString(key: String, value: String) {
    NSUserDefaults.standardUserDefaults.setObject(value, key)
}

actual fun KMMContext.getString(key: String): String? {
    return NSUserDefaults.standardUserDefaults.stringForKey(key)
}

actual fun KMMContext.putArray(key: String, value: Array<String>) {
    NSUserDefaults.standardUserDefaults.setObject(value.toList(), key)
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