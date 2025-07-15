package ai.lerna.multiplatform.service

import korlibs.io.file.VfsOpenMode
import korlibs.io.file.std.applicationDataVfs
import korlibs.io.stream.writeString
import io.github.aakira.napier.Napier
//import korlibs.io.lang.FileNotFoundException
import korlibs.io.lang.IOException

class FileUtil {
	private val LIN_ACCELARATOR_STD_COLUMN = 2

	private fun isNotValidLogs(lines: List<String>): Boolean {
		return try {
			(lines.last().split(',')[LIN_ACCELARATOR_STD_COLUMN] == "0")
		} catch (e: Exception) {
			true
		}
	}

	suspend fun mergeFiles(storage: Storage, fileName: String, filesPrefix: String): Long {

		var fileSize = 0L
		try {
			val osw = try {
				applicationDataVfs[fileName].open(VfsOpenMode.APPEND)
			} catch (e: IOException) {
				applicationDataVfs[fileName].open(VfsOpenMode.CREATE_OR_TRUNCATE)
			}
			osw.setPosition(osw.size())
			for (i in 0 .. storage.getSessionID()) {
				try {
					val sensorFile = applicationDataVfs["$filesPrefix$i.csv"]
					//ToDo: Should be optimized to avoid convert all items to list
					val lines = sensorFile.readLines().filter { it.isNotEmpty() }.toList().takeLast(100)
					if (isNotValidLogs(lines)) {
						sensorFile.delete()
						continue
					}
					for (line in lines) {
						osw.writeString("$line\n")
					}
					sensorFile.delete()
				} catch (e: IOException) {
					Napier.d("File not found: $filesPrefix$i.csv", null, "LernaFL")
					continue
				}
			}
			fileSize = osw.size()
			Napier.d("Filesize: $fileSize", null, "LernaFL")
			osw.close()
		} catch (e: Exception) {
			Napier.d("Error: ${e.message}", null, "LernaFL")
		}
		return fileSize
	}

	suspend fun commitToFile(sessionID: Int, filesPrefix: String, record: String) {
		val sensorFile = try {
			applicationDataVfs["$filesPrefix$sessionID.csv"].open(VfsOpenMode.APPEND)
		} catch (e: IOException) {
			applicationDataVfs["$filesPrefix$sessionID.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		}
		sensorFile.setPosition(sensorFile.size())
		sensorFile.writeString(record)
		sensorFile.close()
	}

	suspend fun cleanUp(sessionId: Int, threshold: Long = 50000000L) {
		var totalSize = 0L
		var fileDeleted = false
		if (applicationDataVfs["sensorLog$sessionId.csv"].exists()) {
			Napier.d("Cleaning unfinished log file sensorLog$sessionId.csv", null, "Lerna")
			applicationDataVfs["sensorLog$sessionId.csv"].delete()
		}
		for (i in sessionId - 1 downTo 0) {
			try {
				totalSize += applicationDataVfs["sensorLog$i.csv"].size()
				if (totalSize > threshold) {
					applicationDataVfs["sensorLog$i.csv"].delete()
					fileDeleted = true
				}
			} catch (e: IOException) {
				continue
			}
		}

		try {
			totalSize += applicationDataVfs["mldata.csv"].size()
			if (totalSize > threshold) {
				applicationDataVfs["mldata.csv"].delete()
				fileDeleted = true
			}
		} catch (_: IOException) {
		}
		Napier.d("Internal file size: $totalSize", null, "Lerna")
		if (fileDeleted) {
			Napier.d("Cleaning internal storage...", null, "Lerna")
		}
	}
}
