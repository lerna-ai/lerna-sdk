package ai.lerna.multiplatform

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
	override val name: String = UIDevice.currentDevice.systemName() + "_" + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
