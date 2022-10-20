package ai.lerna.multiplatform.config

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


internal class userIDTest{

    private lateinit var context: KMMContext

    @BeforeTest
    fun setUp() {
        context = KMMContext()
    }

    @Test
    fun testUUID(){
        val u_id = userID().getUniqueId(context)
        assertEquals(u_id, userID().getUniqueId(context))
    }
}