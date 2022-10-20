package ai.lerna.multiplatform.config

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals


@RunWith(AndroidJUnit4::class)
class userIDTest{
    @Test
    fun testUUID(){
        val u_id = userID().getUniqueId(ApplicationProvider.getApplicationContext())
        assertEquals(u_id, userID().getUniqueId(ApplicationProvider.getApplicationContext()))
    }
}