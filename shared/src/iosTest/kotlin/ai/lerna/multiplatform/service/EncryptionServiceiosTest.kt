package ai.lerna.multiplatform.service

import kotlin.test.Test

internal class EncryptionServiceiosTest {
	private val encryptionServiceTest: EncryptionServiceTest = EncryptionServiceTest()

	@Test
	fun encrypt() {
		encryptionServiceTest.encrypt()
	}

	@Test
	fun decrypt() {
		encryptionServiceTest.decrypt()
	}

	@Test
	fun encryptDecrypt() {
		encryptionServiceTest.encryptDecrypt()
	}

	@Test
	fun encryptGreek() {
		encryptionServiceTest.encryptGreek()
	}

	@Test
	fun decryptGreek() {
		encryptionServiceTest.decryptGreek()
	}

	@Test
	fun encryptDecryptGreek() {
		encryptionServiceTest.encryptDecryptGreek()
	}

	@Test
	fun encryptCyrillic() {
		encryptionServiceTest.encryptCyrillic()
	}

	@Test
	fun decryptCyrillic() {
		encryptionServiceTest.decryptCyrillic()
	}

	@Test
	fun encryptDecryptCyrillic() {
		encryptionServiceTest.encryptDecryptCyrillic()
	}
}