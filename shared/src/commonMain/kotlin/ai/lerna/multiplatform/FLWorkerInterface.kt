package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext

expect class FLWorkerInterface(_context: KMMContext) {
	fun startFL(token: String, uniqueID: Long)
}