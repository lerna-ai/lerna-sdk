package ai.lerna.multiplatform.config.preference


import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.KMMPreference
import platform.darwin.NSObject
import kotlin.test.*

internal class PreferenceTest {

    private lateinit var preferences: KMMPreference

    @BeforeTest
    fun setUp() {
        val context: KMMContext = KMMContext()
        preferences = KMMPreference(context)
    }

    @Test
    fun notContains() {
        // Given
        val key = "NotExists"
        // When
        val result = preferences.contains(key)
        // Then
        assertEquals(result, false)
    }

    @Test
    fun contains() {
        // Given
        val key = "Contains"
        val value = 1
        // When
        preferences.put(key, value)
        val result = preferences.contains(key)
        // Then
        assertEquals(result, true)
    }

    @Test
    fun testIntNotExists() {
        // Given
        val key = "NoIntValue"
        // When
        val result = preferences.getInt(key, 123)
        // Then
        assertEquals(result, 123)
    }

    @Test
    fun testInt() {
        // Given
        val key = "IntValue"
        val value = 1
        // When
        preferences.put(key, value)
        val result = preferences.getInt(key, 0)
        // Then
        assertEquals(result, 1)
    }

    @Test
    fun testStringNotExists() {
        // Given
        val key = "NoStringValue"
        // When
        val result = preferences.getString(key)
        // Then
        assertNull(result)
    }

    @Test
    fun testString() {
        // Given
        val key = "StringValue"
        val value = "value"
        // When
        preferences.put(key, value)
        val result = preferences.getString(key)
        // Then
        assertEquals(result, "value")
    }

    @Test
    fun testBoolNotExists() {
        // Given
        val key = "NoBoolValue"
        // When
        val result = preferences.getBool(key, false)
        // Then
        assertEquals(result, false)
    }

    @Test
    fun testBool() {
        // Given
        val key = "BoolValue"
        val value = true
        // When
        preferences.put(key, value)
        val result = preferences.getBool(key, false)
        // Then
        assertEquals(result, true)
    }

    @Test
    fun testFloatNotExists() {
        // Given
        val key = "NoFloatValue"
        // When
        val result = preferences.getFloat(key, 5.6f)
        // Then
        assertEquals(result, 5.6f)
    }

    @Test
    fun testFloat() {
        // Given
        val key = "FloatValue"
        val value = 1.2f
        // When
        preferences.put(key, value)
        val result = preferences.getFloat(key, 5.6f)
        // Then
        assertEquals(result, 1.2f)
    }

    @Test
    fun testArrayNotExists() {
        // Given
        val key = "NoArrayValue"
        // When
        val result = preferences.getArray(key)
        // Then
        assertNull(result)
    }

    @Test
    fun testArray() {
        // Given
        val key = "ArrayValue"
        val value = arrayOf("alpha", "bravo", "charlie", "delta", "echo")
        // When
        preferences.put(key, value)
        val result = preferences.getArray(key)
        println(result.contentToString())
        // Then
        assertNotNull(result)
        assertEquals(result.size, 5)
        assertContains(result, "alpha")
        assertContains(result, "bravo")
        assertContains(result, "charlie")
        assertContains(result, "delta")
        assertContains(result, "echo")
    }
}