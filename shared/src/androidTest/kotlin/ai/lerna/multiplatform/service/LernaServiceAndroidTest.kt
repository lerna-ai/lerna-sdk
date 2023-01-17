package ai.lerna.multiplatform.service

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soywiz.korio.android.withAndroidContext
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
internal class LernaServiceAndroidTest {
	private val lernaServiceTest: LernaServiceTest = LernaServiceTest(ApplicationProvider.getApplicationContext())

	@Test
	fun updateFileLastSession() {
		runBlocking {
			withAndroidContext(ApplicationProvider.getApplicationContext()) {
				lernaServiceTest.updateFileLastSession()
			}
		}

	}
}