package ai.lerna.multiplatform.service

import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.tempVfs
import com.soywiz.korio.lang.FileNotFoundException
import com.soywiz.korio.stream.writeString
import io.github.aakira.napier.Napier

class FileUtil {
	private val LIN_ACCELARATOR_STD_COLUMN = 19

	private fun isNotValidLogs(lines: List<String>): Boolean {
		return try {
			(lines.last().split(',')[LIN_ACCELARATOR_STD_COLUMN] == "0")
		} catch (e: Exception) {
			true
		}
	}

	suspend fun mergeFiles(storage: Storage): Long {
		var fileSize = 0L
		try {
			val osw = try {
				tempVfs["mldata.csv"].open(VfsOpenMode.WRITE)
			} catch (e: FileNotFoundException) {
				tempVfs["mldata.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
			}
			osw.setPosition(osw.size())
			for (i in 0 .. storage.getSessionID()) {
				try {
					val sensorFile = tempVfs["sensorLog$i.csv"]
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
				} catch (e: FileNotFoundException) {
					Napier.d("File not found: sensorLog$i.csv", null, "LernaFL")
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

	suspend fun commitToFile(sessionID: Int, record: String) {
		val sensorFile = try {
			tempVfs["sensorLog$sessionID.csv"].open(VfsOpenMode.WRITE)
		} catch (e: FileNotFoundException) {
			tempVfs["sensorLog$sessionID.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		}
		sensorFile.setPosition(sensorFile.size())
		sensorFile.writeString(record)
		sensorFile.close()
	}

	suspend fun cleanUp(sessionId: Int, threshold: Long = 50000000L) {
		var totalSize = 0L
		var fileDeleted = false
		if (tempVfs["sensorLog$sessionId.csv"].exists()) {
			Napier.d("Cleaning unfinished log file sensorLog$sessionId.csv", null, "Lerna")
			tempVfs["sensorLog$sessionId.csv"].delete()
		}
		for (i in sessionId - 1 downTo 0) {
			try {
				totalSize += tempVfs["sensorLog$i.csv"].size()
				if (totalSize > threshold) {
					tempVfs["sensorLog$i.csv"].delete()
					fileDeleted = true
				}
			} catch (e: FileNotFoundException) {
				continue
			}
		}

		try {
			totalSize += tempVfs["mldata.csv"].size()
			if (totalSize > threshold) {
				tempVfs["mldata.csv"].delete()
				fileDeleted = true
			}
		} catch (_: FileNotFoundException) {
		}
		Napier.d("Internal file size: $totalSize", null, "Lerna")
		if (fileDeleted) {
			Napier.d("Cleaning internal storage...", null, "Lerna")
		}
	}
}
