package ai.lerna.multiplatform.service

import com.soywiz.korio.file.VfsOpenMode
import com.soywiz.korio.file.std.cacheVfs
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
			val osw = cacheVfs["mldata.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
			for (i in 0 until storage.getSessionID()) {
				try {
					val sensorFile = cacheVfs["sensorLog$i.csv"]
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
			fileSize = osw.size() ?: 0
			storage.putSessionID(0)
			Napier.d("Filesize: $fileSize", null, "LernaFL")
			osw.close()
		} catch (e: Exception) {
			Napier.d("Error: ${e.message}", null, "LernaFL")
		}
		return fileSize
	}

	suspend fun commitToFile(sessionID: Int, record: String) {
		val sensorFile = try {
			cacheVfs["sensorLog$sessionID.csv"].open(VfsOpenMode.WRITE)
		} catch (e: FileNotFoundException) {
			cacheVfs["sensorLog$sessionID.csv"].open(VfsOpenMode.CREATE_OR_TRUNCATE)
		}
		sensorFile.setPosition(sensorFile.size())
		sensorFile.writeString(record)
		sensorFile.close()
	}
}
