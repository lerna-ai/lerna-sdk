package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import android.provider.Settings
import io.ktor.util.date.*
import java.util.*

actual class Sensors actual constructor(_context: KMMContext, _modelData: ModelData) :
    SensorEventListener, SensorInterface {

    private var context = _context
    private var modelData = _modelData
    private var mSensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var sAccelerometer: Sensor? = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var sGravity: Sensor? = mSensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private var sMagnetic: Sensor? = mSensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private var sRotation: Sensor? = mSensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    //private var sProximity: Sensor? = mSensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private var sGyroscope: Sensor? = mSensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private var sLinAcc: Sensor? = mSensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private var sLight: Sensor? = mSensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private var rawAccelerometer: FloatArray? = null
    private var rawRotation: FloatArray? = null
    private var rawGravity: FloatArray? = null
    private var rawMagnetic: FloatArray? = null
    private var rawRotationMatrix = FloatArray(9)
    private var orientation = FloatArray(3)
    private var heading: Double = 0.0

    private var _isEnabled = false
    actual override val isEnabled: Boolean
        get() = _isEnabled

    private var _data = MutableStateFlow<SensorData?>(null)
    actual override val data: CommonFlow<SensorDataInterface?>
        get() = _data.asCommonFlow()

    actual override fun start() {
        _isEnabled = true
        mSensorManager?.registerListener(this, sAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager?.registerListener(this, sGravity, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager?.registerListener(this, sMagnetic, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager?.registerListener(this, sRotation, SensorManager.SENSOR_DELAY_NORMAL)
        //mSensorManager?.registerListener(this, sProximity, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager?.registerListener(this, sGyroscope, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager?.registerListener(this, sLinAcc, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager?.registerListener(this, sLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    actual override fun stop() {
        _isEnabled = false
        mSensorManager?.unregisterListener(this, sAccelerometer)
        mSensorManager?.unregisterListener(this, sGravity)
        mSensorManager?.unregisterListener(this, sMagnetic)
        mSensorManager?.unregisterListener(this, sRotation)
        //mSensorManager?.unregisterListener(this, sProximity)
        mSensorManager?.unregisterListener(this, sGyroscope)
        mSensorManager?.unregisterListener(this, sLinAcc)
        mSensorManager?.unregisterListener(this, sLight)
    }

    actual override fun updateData() {
        modelData.setDateTime(GMTDate().hours, GMTDate().dayOfWeek.ordinal)

        modelData.setPhones(isWiredHeadsetOn())

        modelData.setOrientation(getOrientation())

        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { iFilter ->
            context.registerReceiver(null, iFilter)
        }
        modelData.setBatteryChargingState(batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager? ?: return
        modelData.setAudioActivity(
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat(),
            audioManager.isBluetoothScoOn,
            audioManager.isMusicActive,
            audioManager.isSpeakerphoneOn,
            audioManager.isWiredHeadsetOn
        )

        modelData.setWifiConnected(isConnectionUnmetered(context, true))

        modelData.setHistory()
    }

    @SuppressLint("MissingPermission")
    private fun isConnectionUnmetered(context: Context, forceWithNotPermission: Boolean = false): Boolean {
        if (context.checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            return forceWithNotPermission
        }
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivityManager?.run {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                if (hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)) {
                    return true
                }
            }
        }
        return false
    }

    private fun getOrientation(): Float {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 1F else 0F
    }

    private fun getBrightness(): Float {
        return Settings.System.getFloat(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS).div(255f)
    }

    private fun isWiredHeadsetOn(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS or AudioManager.GET_DEVICES_OUTPUTS)
        for (deviceInfo in audioDevices) {
            if (deviceInfo.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                || deviceInfo.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                || deviceInfo.type == AudioDeviceInfo.TYPE_USB_HEADSET
                || deviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                || deviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET
            ) {
                return true
            }
        }
        return false
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (!isEnabled) return

        val values = p0!!.values.copyOf()
        val maxRange = if (p0.sensor.maximumRange > 0) p0.sensor.maximumRange else 1f

        when (p0.sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD -> {
                modelData.setMagneticField(values[0], values[1], values[2])
                rawMagnetic = values
            }
            Sensor.TYPE_GRAVITY -> {
                rawGravity = values
            }
            Sensor.TYPE_ACCELEROMETER -> {
                rawAccelerometer = values
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                rawRotation = values
                SensorManager.getRotationMatrixFromVector(rawRotationMatrix, values)
                heading = ((Math.toDegrees(
                    SensorManager.getOrientation(
                        rawRotationMatrix,
                        orientation
                    )[0].toDouble()
                ) + 360).toInt() % 360).toDouble()
            }
            //Sensor.TYPE_PROXIMITY -> modelData.setProximity(values[0].div(maxRange))
            Sensor.TYPE_GYROSCOPE -> modelData.setGyroscope(values[0], values[1], values[2])
            Sensor.TYPE_LINEAR_ACCELERATION -> modelData.setLinAcceleration(values[0], values[1], values[2])
            Sensor.TYPE_LIGHT -> modelData.setLight(values[0].div(maxRange))
        }

        if (rawMagnetic != null && rawGravity != null && rawAccelerometer != null && rawRotation != null) {

            SensorManager.getRotationMatrix(rawRotationMatrix, null, rawGravity, rawMagnetic)
            heading = ((Math.toDegrees(
                SensorManager.getOrientation(
                    rawRotationMatrix,
                    orientation
                )[0].toDouble()
            ) + 360).toInt() % 360).toDouble()

            val user = AccelerometerData(
                rawAccelerometer!![0].toDouble(),
                rawAccelerometer!![1].toDouble(),
                rawAccelerometer!![2].toDouble()
            )
            val gravity = AccelerometerData(
                rawGravity!![0].toDouble(),
                rawGravity!![1].toDouble(),
                rawGravity!![2].toDouble()
            )

            _data.value = SensorData(heading, user, gravity)
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}