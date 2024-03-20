package ai.lerna.multiplatform.service

import com.soywiz.krypto.AES.Companion.decryptAesEcb
import com.soywiz.krypto.AES.Companion.encryptAesEcb
import com.soywiz.krypto.Padding
import com.soywiz.krypto.encoding.Hex
import com.soywiz.korio.lang.toByteArray

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
