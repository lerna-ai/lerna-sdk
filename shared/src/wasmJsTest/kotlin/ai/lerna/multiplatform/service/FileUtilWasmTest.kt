package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.config.KMMContext
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

internal class FileUtilWasmTest {
	private val fileUtilTest: FileUtilTest = FileUtilTest()
	private lateinit var storage: Storage


	@BeforeTest
	fun setUp() {
		val context: KMMContext = KMMContext()
		storage = StorageImpl(context)
	}

	@Test
	fun mergeFiles() = runTest {
			fileUtilTest.mergeFiles(storage)
			fileUtilTest.mergeFilesTruncated(storage)
		}


	@Test
	fun cleanup() = runTest {
			fileUtilTest.cleanup(storage)
			fileUtilTest.cleanup_with_mldata(storage)
			fileUtilTest.cleanup_delete_mldata(storage)
			fileUtilTest.cleanup_delete_mldata_and_sensor2(storage)
		}

}