package ai.lerna.multiplatform

import ai.lerna.multiplatform.config.KMMContext
import ai.lerna.multiplatform.utils.DateUtil
import io.ktor.util.date.*
import kotlinx.cinterop.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.AVFAudio.*
import platform.CoreMotion.CMAcceleration
import platform.CoreMotion.CMAttitudeReferenceFrameXMagneticNorthZVertical
import platform.CoreMotion.CMDeviceMotion
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import platform.SystemConfiguration.*
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryState
import platform.UIKit.UIDeviceOrientationIsLandscape
import platform.UIKit.UIScreen
import kotlin.math.PI

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class Sensors actual constructor(_context: KMMContext, _modelData: ModelData) : SensorInterface {

    private var _data = Channel<SensorDataInterface?>(Channel.BUFFERED)
    private var modelData = _modelData

    actual override val data: CommonFlow<SensorDataInterface?>
        get() = _data.consumeAsFlow().asCommonFlow()
    private var _isEnabled = false
    actual override val isEnabled: Boolean
        get() = _isEnabled

    private val scope = CoroutineScope(UIDispatcher())
    private val queue = NSOperationQueue.mainQueue
    private val manager = CMMotionManager().apply {
        deviceMotionUpdateInterval = 3.0/10.0 //20 / 1000.0
    }
    private val myDevice = UIDevice.currentDevice
    private val myScreen = UIScreen.mainScreen
    private val audioSession = AVAudioSession.sharedInstance()


    actual override fun start() {
        myDevice.batteryMonitoringEnabled = true
        myDevice.proximityMonitoringEnabled = true
        audioSession.setActive(true, null)

        _isEnabled = true
        manager.startDeviceMotionUpdatesUsingReferenceFrame(
            CMAttitudeReferenceFrameXMagneticNorthZVertical, queue
        ) { motion, error ->
            if (error != null) {
                return@startDeviceMotionUpdatesUsingReferenceFrame
            }
            motion?.let {
                scope.launch {
                    val converted = convertIntoSensorData(it)
                    _data.send(converted)
                }
            }
        }
    }

    actual override fun stop() {
        _isEnabled = false

        manager.stopDeviceMotionUpdates()
    }

    private fun convertIntoSensorData(motion: CMDeviceMotion?): SensorDataInterface? {
        if (motion == null) return null
        val user = motion.userAcceleration
        val gravity = motion.gravity
        val heading = motion.heading
        val attitude = motion.attitude
        val magnetic = motion.magneticField

        magnetic.useContents {
            modelData.setMagneticField(
                this.field.x.toFloat(),
                this.field.y.toFloat(), 
                this.field.z.toFloat())
        }
        modelData.setGyroscope(
            convertHztoSI(attitude.roll).toFloat(),
            convertHztoSI(attitude.pitch).toFloat(),
            convertHztoSI(attitude.yaw).toFloat()
        )
        user.useContents {
            modelData.setLinAcceleration(
                convertGtoSI(this.x).toFloat(),
                convertGtoSI(this.y).toFloat(),
                convertGtoSI(this.z).toFloat())
        }
        return SensorData(heading, convertAcceleration(user), convertAcceleration(gravity))
    }

    private fun convertAcceleration(acceleration: CValue<CMAcceleration>): AccelerometerInterface {
        acceleration.useContents {
            return AccelerometerData(this.x, this.y, this.z)
        }
    }

    actual override fun updateData() {
		val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        modelData.setDateTime(now.hour, now.dayOfWeek.ordinal)

        val currentRoute = audioSession.currentRoute()
        val portDescriptionArray = currentRoute.outputs() as List<AVAudioSessionPortDescription>

        val headPhonePortDescriptionArray = portDescriptionArray.filter {  it.portType == AVAudioSessionPortHeadphones  }
        val isHeadphoneConnected = headPhonePortDescriptionArray.isNotEmpty()
        modelData.setPhones(isHeadphoneConnected)

        val bluetoothDescriptionArray = portDescriptionArray.filter {  (it.portType == AVAudioSessionPortBluetoothA2DP) || (it.portType == AVAudioSessionPortBluetoothHFP) || (it.portType == AVAudioSessionPortBluetoothLE) }
        val isBluetoothConnected = bluetoothDescriptionArray.isNotEmpty()

        val speakerDescriptionArray = portDescriptionArray.filter {  it.portType == AVAudioSessionPortBuiltInSpeaker }
        val isSpeakerOn = speakerDescriptionArray.isNotEmpty()

        val headsetDescriptionArray = portDescriptionArray.filter {  it.portType == AVAudioSessionPortBuiltInReceiver }
        val isHeadsetOn = headsetDescriptionArray.isNotEmpty()

        val music = audioSession.isOtherAudioPlaying()

        val volume = audioSession.outputVolume()

        modelData.setAudioActivity(volume, isBluetoothConnected, music, isSpeakerOn, isHeadsetOn)

        val wifi = isWifiConnected()
        modelData.setWifiConnected(wifi)

        //val proximity = if (myDevice.proximityState) {1.0f} else {0.0f}
        //modelData.setProximity(proximity)

        val orientation = if (UIDeviceOrientationIsLandscape(myDevice.orientation)) {1.0} else {0.0}
        modelData.setOrientation(orientation.toFloat())

        val battery = if (myDevice.batteryState == UIDeviceBatteryState.UIDeviceBatteryStateCharging || myDevice.batteryState == UIDeviceBatteryState.UIDeviceBatteryStateFull) {1} else {0}
        modelData.setBatteryChargingState(battery)

        modelData.setLight(myScreen.brightness.toFloat())

        modelData.setHistory(DateUtil().now())
        //Napier.d("Is wifi connected?: ${wifi}, Is music playing?: ${music}, Is headphone connected?: ${isHeadphoneConnected}, Is bluetooth connected?: ${isBluetoothConnected}, Is speaker on?: ${isSpeakerOn}, Is headset on?: ${isHeadsetOn}, Volume: $volume, Proximity: $proximity, orientation: $orientation, battery: $battery, brightness: ${myScreen.brightness}", null, "Lerna Sensors")
    }

    private fun isWifiConnected(): Boolean {
        memScoped {
            val reachability = SCNetworkReachabilityCreateWithName(null, "8.8.8.8")
            val flags = alloc<SCNetworkReachabilityFlagsVar>()
            val success = SCNetworkReachabilityGetFlags(reachability, flags.ptr)
            val isReachable =
                success && (flags.value.toInt() and kSCNetworkFlagsReachable.toInt() != 0) && flags.value.toInt() and kSCNetworkFlagsConnectionRequired.toInt() == 0
            val isWifi = success && (flags.value.toInt() and kSCNetworkReachabilityFlagsIsWWAN.toInt() == 0)
            return isReachable && isWifi
        }
    }

    /**
     * Calculated based on a conversion factor of 1G = 9.80665m/s2, and 5th digit is rounded.
     */
    private fun convertGtoSI(gValue: Double): Double {
        return gValue * 9.80665
    }

    /**
     * Calculated based on a conversion factor of 1 radians per second = Hz/(2 × π).
     */
    private fun convertHztoSI(rotation: Double): Double {
        return -rotation * 2.0 / PI
    }
}


