package ai.lerna.multiplatform.config.preference

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.config.KMMPreference
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PreferenceTest {

	private lateinit var preferences: KMMPreference

	@Before
	fun setUp() {
		val context: KMMContext = ApplicationProvider.getApplicationContext()
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
		val key = "IntValue"
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
		val key = "StringValue"
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
		val key = "BoolValue"
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
	fun testDoubleNotExists() {
		// Given
		val key = "DoubleValue"
		// When
		val result = preferences.getDouble(key, 5.6)
		// Then
		assertEquals(result, 5.6)
	}

	@Test
	fun testDouble() {
		// Given
		val key = "DoubleValue"
		val value = 1.2
		// When
		preferences.put(key, value)
		val result = preferences.getDouble(key, 5.6)
		// Then
		assertEquals(result, 1.2)
	}

	@Test
	fun testArrayNotExists() {
		// Given
		val key = "ArrayValue"
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
		// Then
		assertEquals(result?.size, 5)
		assertEquals(result?.get(0), "alpha")
		assertEquals(result?.get(1), "bravo")
		assertEquals(result?.get(2), "charlie")
		assertEquals(result?.get(3), "delta")
		assertEquals(result?.get(4), "echo")
	}
}