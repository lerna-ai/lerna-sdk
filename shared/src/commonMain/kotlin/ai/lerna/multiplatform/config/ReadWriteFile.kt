package ai.lerna.multiplatform.config

import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

expect class ReadWriteFile constructor(){
    fun read(path : String, filename: String) : List<DoubleArray>
    fun save(path : String, filename : String, nbLine : Int)
}