package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class LernaServiceiosTest {
	private lateinit var lernaServiceTest: LernaServiceTest

	@BeforeTest
	fun setUp() {
		val context: KMMContext = KMMContext()
		lernaServiceTest = LernaServiceTest(context)
	}

	@Test
	fun updateFileLastSession() {
		runBlocking {
			lernaServiceTest.updateFileLastSession()
		}
	}
}