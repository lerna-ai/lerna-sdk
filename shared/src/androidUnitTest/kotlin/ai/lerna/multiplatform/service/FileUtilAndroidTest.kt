package ai.lerna.multiplatform.service

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soywiz.korio.android.withAndroidContext
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
internal class FileUtilAndroidTest {
	private val fileUtilTest: FileUtilTest = FileUtilTest()
	private val storage: Storage = StorageImpl(ApplicationProvider.getApplicationContext())

	@Test
	fun mergeFiles() {
		runBlocking {
			withAndroidContext(ApplicationProvider.getApplicationContext()) {
				fileUtilTest.mergeFiles(storage)
				fileUtilTest.mergeFilesTruncated(storage)
			}
		}

	}

	@Test
	fun cleanup() {
		runBlocking {
			withAndroidContext(ApplicationProvider.getApplicationContext()) {
				fileUtilTest.cleanup(storage)
				fileUtilTest.cleanup_with_mldata(storage)
				fileUtilTest.cleanup_delete_mldata(storage)
				fileUtilTest.cleanup_delete_mldata_and_sensor2(storage)
			}
		}

	}
}
