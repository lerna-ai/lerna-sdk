package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.config.KMMContext

internal class LernaServiceTest(kmmContext: KMMContext) {
	private val lernaService: LernaService = LernaService(kmmContext, "", 123L, true)

	suspend fun updateFileLastSession() {
		// Given
		// When
		// Then
	}
}