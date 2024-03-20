package ai.lerna.multiplatform.service

import kotlin.test.assertEquals

internal class EncryptionServiceTest() {
	private val key = "924103acadba54b6e153bdabcc6c920f"
	private val encryptionService: EncryptionService = EncryptionService(key)

	fun encrypt() {
		// Given
		val message = "Hello World!"
		// When
		val result = encryptionService.encrypt(message)
		// Then
		assertEquals("947126cdd74bd00a18982fde10818a3d", result)
	}

	fun decrypt() {
		// Given
		val encryptedMessage = "947126cdd74bd00a18982fde10818a3d"
		// When
		val result = encryptionService.decrypt(encryptedMessage)
		// Then
		assertEquals("Hello World!", result)
	}

	fun encryptDecrypt() {
		// Given
		val message = "Hello World!"
		// When
		val encryptedMessage = encryptionService.encrypt(message)
		val result = encryptionService.decrypt(encryptedMessage)
		// Then
		assertEquals("Hello World!", result)
	}

	fun encryptGreek() {
		// Given
		val message = "Γειά σου κόσμε!"
		// When
		val result = encryptionService.encrypt(message)
		// Then
		assertEquals("5e3157ecc28dd81cdc0650df329ae1114dda23598a89c248cab5fbd66b38ce64", result)
	}

	fun decryptGreek() {
		// Given
		val encryptedMessage = "5e3157ecc28dd81cdc0650df329ae1114dda23598a89c248cab5fbd66b38ce64"
		// When
		val result = encryptionService.decrypt(encryptedMessage)
		// Then
		assertEquals("Γειά σου κόσμε!", result)
	}

	fun encryptDecryptGreek() {
		// Given
		val message = "Γειά σου κόσμε!"
		// When
		val encryptedMessage = encryptionService.encrypt(message)
		val result = encryptionService.decrypt(encryptedMessage)
		// Then
		assertEquals("Γειά σου κόσμε!", result)
	}

	fun encryptCyrillic() {
		// Given
		val message = "Привет, мир!"
		// When
		val result = encryptionService.encrypt(message)
		// Then
		assertEquals("e5074d4260fb18742b25d4225bd905bc2c061aefb6a05de596c9071b41a7934d", result)
	}

	fun decryptCyrillic() {
		// Given
		val encryptedMessage = "e5074d4260fb18742b25d4225bd905bc2c061aefb6a05de596c9071b41a7934d"
		// When
		val result = encryptionService.decrypt(encryptedMessage)
		// Then
		assertEquals("Привет, мир!", result)
	}

	fun encryptDecryptCyrillic() {
		// Given
		val message = "Привет, мир!"
		// When
		val encryptedMessage = encryptionService.encrypt(message)
		val result = encryptionService.decrypt(encryptedMessage)
		// Then
		assertEquals("Привет, мир!", result)
	}
}
