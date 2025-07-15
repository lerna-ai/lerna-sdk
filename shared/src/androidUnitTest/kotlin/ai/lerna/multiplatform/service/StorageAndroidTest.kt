package ai.lerna.multiplatform.service

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import korlibs.io.android.withAndroidContext
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
internal class StorageAndroidTest {
	private val storage: Storage = StorageImpl(ApplicationProvider.getApplicationContext())

	@Test
	fun storeClasses() {
		runBlocking {
			withAndroidContext(ApplicationProvider.getApplicationContext()) {
				val classes = mutableMapOf<String, MutableList<String>>(Pair("success", mutableListOf("class1", "class2")))
				storage.putClasses(classes)
				storage.getClasses()?.get("success")?.let { assertEquals(it[1],"class2") }
			}
		}

	}

}
