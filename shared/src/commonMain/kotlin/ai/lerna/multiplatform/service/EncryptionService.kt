package ai.lerna.multiplatform.service

import korlibs.crypto.AES.Companion.decryptAesEcb
import korlibs.crypto.AES.Companion.encryptAesEcb
import korlibs.crypto.Padding
import korlibs.encoding.Hex
import korlibs.io.lang.toByteArray

class EncryptionService(key: String) {
	private	val encryptionKey = key.toByteArray()

	internal fun encrypt(message: String) : String {
		val originalString = message.toByteArray()
		val encryptedString = encryptAesEcb(originalString, encryptionKey, Padding.PKCS7Padding)
		return Hex.encode(encryptedString)
	}

	internal fun decrypt(encryptedMessage: String) : String {
		val encryptedString = Hex.decode(encryptedMessage)
		val decryptedString = decryptAesEcb(encryptedString, encryptionKey, Padding.PKCS7Padding)
		return decryptedString.decodeToString()
	}
}
