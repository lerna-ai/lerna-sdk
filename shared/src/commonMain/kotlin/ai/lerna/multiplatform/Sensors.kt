package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext

interface AccelerometerInterface {
    val x: Double
    val y: Double
    val z: Double
}

data class AccelerometerData(
    override val x: Double,
    override val y: Double,
    override val z: Double
) : AccelerometerInterface


interface SensorDataInterface {
    val heading: Double
    val sensor: AccelerometerInterface
    val gravity: AccelerometerInterface?
}

data class SensorData(
    override val heading: Double,
    override val sensor: AccelerometerInterface,
    override val gravity: AccelerometerInterface? = null
) : SensorDataInterface

interface SensorInterface {
    val data: CommonFlow<SensorDataInterface?>
    val isEnabled: Boolean
    fun start()
    fun stop()
    fun updateData()
}

expect class Sensors(_context: KMMContext, _modelData: ModelData) : SensorInterface {
    override val data: CommonFlow<SensorDataInterface?>
    override val isEnabled: Boolean
    override fun start()
    override fun stop()
    override fun updateData()
}