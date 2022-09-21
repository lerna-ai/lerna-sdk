package ai.lerna.multiplatform.config

import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import platform.Foundation.*

actual class ReadWriteFile {
    actual fun read(path : String, filename: String) : List<DoubleArray> {
        var list = mutableListOf<DoubleArray>()
        val string = NSString.stringWithContentsOfFile(filename, NSUTF8StringEncoding, null) ?: return list
        string.lines().forEach {row ->
            list.add(row.split(",").map { it.toDouble() }.toDoubleArray())
        }
        return list
    }

    actual fun save(path : String, filename : String, nbLine : Int) {
        val result = (1..nbLine).map {
            "string $it"
        }
        (result as NSString).writeToFile(path, true, NSUTF8StringEncoding, null)
    }
}