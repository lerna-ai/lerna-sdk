package ai.lerna.multiplatform.service

import ai.lerna.multiplatform.config.KMMContext
import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.cacheVfs
import com.soywiz.korio.stream.writeString
import kotlin.test.assertEquals

internal class LernaServiceTest(kmmContext: KMMContext) {
	private val lernaService: LernaService = LernaService(kmmContext, "", 123L, true)

	suspend fun updateFileLastSession() {
		// Given
		val sessionId = 0
		val successValue = 1
		val sensorFile = cacheVfs["sensorLog$sessionId.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()
		// When
		lernaService.updateFileLastSession(sessionId, successValue)
		// Then
		val mlData = cacheVfs["sensorLog$sessionId.csv"].readLines().toList().filter { it.isNotEmpty() }
		assertEquals(5, mlData.size)
		assertEquals("0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1", mlData[0])
		assertEquals("1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1", mlData[1])
		assertEquals("2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1", mlData[2])
		assertEquals("3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1", mlData[3])
		assertEquals("4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1", mlData[4])
	}
}