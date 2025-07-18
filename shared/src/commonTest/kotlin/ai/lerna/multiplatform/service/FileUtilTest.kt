package ai.lerna.multiplatform.service

import korlibs.io.file.VfsOpenMode
import korlibs.io.file.std.applicationDataVfs
import korlibs.io.lang.FileNotFoundException
import korlibs.io.stream.writeString
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FileUtilTest {
	private val fileUtil: FileUtil = FileUtil()

	suspend fun mergeFiles(storage: Storage) {
		// Given
		var sensorFile = applicationDataVfs["sensorLog0.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog0.csv"].open(VfsOpenMode.APPEND)
		sensorFile.setPosition(sensorFile.size())
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()

		sensorFile = applicationDataVfs["sensorLog2.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("1,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("2,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("3,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("4,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()

		storage.putSessionID(3)

		// When
		fileUtil.mergeFiles(storage, "mldata.csv", "sensorLog")
		// Then
		val mlData = applicationDataVfs["mldata.csv"].readLines().toList().filter { it.isNotEmpty() }
		applicationDataVfs["mldata.csv"].delete()
		assertEquals(10, mlData.size)
		assertEquals("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[0])
		assertEquals("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[1])
		assertEquals("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[2])
		assertEquals("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[3])
		assertEquals("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[4])
		assertEquals("0,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[5])
		assertEquals("1,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[6])
		assertEquals("2,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[7])
		assertEquals("3,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[8])
		assertEquals("4,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[9])


	}

	suspend fun mergeFilesTruncated(storage: Storage) {
		// Given
		var sensorFile = applicationDataVfs["sensorLog0.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog0.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0\n")
		sensorFile.close()

		storage.putSessionID(3)
		// When
		fileUtil.mergeFiles(storage, "mldata.csv", "sensorLog")
		// Then
		val mlData = applicationDataVfs["mldata.csv"].readLines().toList().filter { it.isNotEmpty() }
		applicationDataVfs["mldata.csv"].delete()
		assertEquals(2, mlData.size)
		assertEquals("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[0])
		assertEquals("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0", mlData[1])

	}

	suspend fun cleanup(storage: Storage) {
		// Given
		val threshold = 300L
		var sensorFile = applicationDataVfs["sensorLog0.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog1.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()

		storage.putSessionID(3)
		// When
		fileUtil.cleanUp(3, threshold)
		// Then
		var totalSize = 0L
		for (i in 0..storage.getSessionID()) {
			try {
				totalSize += applicationDataVfs["sensorLog$i.csv"].size()
			} catch (e: FileNotFoundException) {
				continue
			}
		}
		try {
			totalSize += applicationDataVfs["mldata.csv"].size()
		} catch (_: FileNotFoundException) {
		}

		assertEquals(200, totalSize)
		assertTrue(totalSize < threshold)
		//assertEquals(2, applicationDataVfs.listNames().size)
		applicationDataVfs["sensorLog0.csv"].delete()
		applicationDataVfs["sensorLog1.csv"].delete()

	}

	suspend fun cleanup_with_mldata(storage: Storage) {
		// Given
		val threshold = 500L
		var sensorFile = applicationDataVfs["sensorLog0.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog1.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		fileUtil.mergeFiles(storage, "mldata.csv", "sensorLog")
		sensorFile = applicationDataVfs["sensorLog2.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog3.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		storage.putSessionID(5)
		// When
		fileUtil.cleanUp(5, threshold)
		// Then
		var totalSize = 0L
		for (i in 0..storage.getSessionID()) {
			try {
				totalSize += applicationDataVfs["sensorLog$i.csv"].size()
			} catch (e: FileNotFoundException) {
				continue
			}
		}
		try {
			totalSize += applicationDataVfs["mldata.csv"].size()
		} catch (_: FileNotFoundException) {
		}

		assertEquals(400, totalSize)
		assertTrue(totalSize < threshold)
		//assertEquals(3, applicationDataVfs.listNames().size)
		applicationDataVfs["sensorLog2.csv"].delete()
		applicationDataVfs["sensorLog3.csv"].delete()
		applicationDataVfs["mldata.csv"].delete()
	}

	suspend fun cleanup_delete_mldata(storage: Storage) {
		// Given
		val threshold = 300L
		var sensorFile = applicationDataVfs["sensorLog0.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog1.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		fileUtil.mergeFiles(storage, "mldata.csv", "sensorLog")
		sensorFile = applicationDataVfs["sensorLog2.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog3.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		storage.putSessionID(5)
		// When
		fileUtil.cleanUp(5, threshold)
		// Then
		var totalSize = 0L
		for (i in 0..storage.getSessionID()) {
			try {
				totalSize += applicationDataVfs["sensorLog$i.csv"].size()
			} catch (e: FileNotFoundException) {
				continue
			}
		}
		try {
			totalSize += applicationDataVfs["mldata.csv"].size()
		} catch (_: FileNotFoundException) {
		}
		assertEquals(200, totalSize)
		assertTrue(totalSize < threshold)
		//assertEquals(2, applicationDataVfs.listNames().size)
		applicationDataVfs["sensorLog3.csv"].delete()
		applicationDataVfs["mldata.csv"].delete()
	}

	suspend fun cleanup_delete_mldata_and_sensor2(storage: Storage) {
		// Given
		val threshold = 100L
		var sensorFile = applicationDataVfs["sensorLog0.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog1.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		fileUtil.mergeFiles(storage, "mldata.csv", "sensorLog")
		sensorFile = applicationDataVfs["sensorLog2.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("2,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		sensorFile = applicationDataVfs["sensorLog3.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		sensorFile.writeString("3,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.writeString("4,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n")
		sensorFile.close()
		storage.putSessionID(5)
		// When
		fileUtil.cleanUp(5, threshold)
		// Then
		var totalSize = 0L
		for (i in 0..storage.getSessionID()) {
			try {
				totalSize += applicationDataVfs["sensorLog$i.csv"].size()
			} catch (e: FileNotFoundException) {
				continue
			}
		}
		try {
			totalSize += applicationDataVfs["mldata.csv"].size()
		} catch (_: FileNotFoundException) {
		}
		assertEquals(80, totalSize)
		assertTrue(totalSize < threshold)
		//assertEquals(1, applicationDataVfs.listNames().size)
		applicationDataVfs["sensorLog3.csv"].delete()
		applicationDataVfs["mldata.csv"].delete()
	}
}
