package ai.lerna.multiplatform.service


import ai.lerna.multiplatform.config.KMMContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class StorageIosTest {
    private lateinit var storage: Storage

    @BeforeTest
    fun setUp() {
        val context: KMMContext = KMMContext()
        storage = StorageImpl(context)
    }

    @Test
    fun testClasses() {
        val classes = mutableMapOf<String, MutableList<String>>(Pair("success", mutableListOf("class1", "class2")))
        storage.putClasses(classes)
        storage.getClasses()?.get("success")?.let { assertEquals(it[1],"class2") }
    }
}