package ai.lerna.multiplatform

import kotlinx.browser.window

class WebPlatform : Platform {
    override val name: String = "Browser_${window.navigator.userAgent}"
}
actual fun getPlatform(): Platform = WebPlatform()