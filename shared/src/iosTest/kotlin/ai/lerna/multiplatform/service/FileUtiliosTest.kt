package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class FileUtiliosTest {
	private val fileUtilTest: FileUtilTest = FileUtilTest()
	private lateinit var storage: Storage


	@BeforeTest
	fun setUp() {
		val context: KMMContext = KMMContext()
		storage = StorageImpl(context)
	}

	@Test
	fun mergeFiles() {
		runBlocking {
			fileUtilTest.mergeFiles(storage)
		}
	}
}