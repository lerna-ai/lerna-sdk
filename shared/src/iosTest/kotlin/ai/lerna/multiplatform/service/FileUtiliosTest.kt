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
			fileUtilTest.mergeFilesTruncated(storage)
		}
	}

	@Test
	fun cleanup() {
		runBlocking {
			fileUtilTest.cleanup(storage)
			fileUtilTest.cleanup_with_mldata(storage)
			fileUtilTest.cleanup_delete_mldata(storage)
			fileUtilTest.cleanup_delete_mldata_and_sensor2(storage)
		}
	}
}
