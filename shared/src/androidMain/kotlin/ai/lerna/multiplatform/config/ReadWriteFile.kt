package ai.lerna.multiplatform.config

import java.io.File
import java.net.URL

actual class ReadWriteFile {
    actual fun read(path : String, filename: String) : List<DoubleArray> {
        val resource: URL = javaClass.classLoader.getResource(filename)
        val file =  File(resource.toURI())
        val list = mutableListOf<DoubleArray>()
        if(file.exists()) {
            file.forEachLine {row ->
                list.add(row.split(",").map { it.toDouble() }.toDoubleArray())
            }
        }
        return list
    }

    actual fun save(path : String, filename : String, nbLine : Int) {
        val file = File("$path/$filename")
        val w = file.writer()
        for(it in 1..nbLine) {
            w.write("write something\n")
        }
        w.close()
    }
}