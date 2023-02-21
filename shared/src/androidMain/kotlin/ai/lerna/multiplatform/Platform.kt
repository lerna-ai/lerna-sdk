package ai.lerna.multiplatform

class AndroidPlatform : Platform {
    override val name: String = "Android_${android.os.Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()